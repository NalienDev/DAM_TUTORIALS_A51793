# CoolWeatherApp — MVVM Rebuild with Jetpack Compose
## Development Report

---

## Overview

This report documents the full process of rebuilding the CoolWeatherApp from a legacy XML-based Android app into a modern MVVM-architected Jetpack Compose application. The app fetches real-time weather data from the [Open-Meteo API](https://open-meteo.com/), displays it in a responsive UI that adapts to portrait and landscape orientations, supports GPS-based location detection, allows manual coordinate entry, provides a map-based location picker, and saves favourite named locations.

The project is organised into three packages following MVVM architecture:

```
dam.a51793.coolweatherapp/
├── data/           ← Model layer: API client and data classes
├── viewmodel/      ← ViewModel and UI state
└── ui/             ← Jetpack Compose composables
```

---

## 1. Data Layer (`data/`)

### 1.1 `WeatherData.kt` — Data Models

The original `WeatherData.kt` used `ArrayList<T>` and lacked `@Serializable` annotations, which are required by Ktor's `kotlinx.serialization` content negotiation plugin.

**Changes made:**
- Added `@Serializable` to every data class (`WeatherData`, `CurrentWeather`, `Hourly`, `Daily`). Without this annotation on **every** class in the hierarchy, the Ktor deserializer throws a `SerializationException` at runtime even if the root class is annotated.
- Changed `ArrayList<T>` to `List<T>` throughout, since `kotlinx.serialization` handles `List` natively without extra configuration.
- Changed all `var` fields to `val` since data is immutable once received from the API.
- `latitude` and `longitude` remain `String` (as returned by the API), with conversion to `Float` handled in the ViewModel.
- `WeatherCodeInfo` is kept here as a plain (non-serializable) data class since it is constructed locally from XML resources, not from JSON.

```kotlin
@Serializable
data class WeatherData(
    val latitude: String,
    val longitude: String,
    val timezone: String,
    val current_weather: CurrentWeather,
    val hourly: Hourly,
    val daily: Daily,
)

@Serializable
data class Daily(val sunrise: List<String>, val sunset: List<String>)

@Serializable
data class CurrentWeather(
    val temperature: Float,
    val windspeed: Float,
    val winddirection: Int,
    val weathercode: Int,
    val time: String,
)

@Serializable
data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Float>,
    val weathercode: List<Int>,
    val pressure_msl: List<Double>,
)
```

### 1.2 `WeatherApiClient.kt` — Ktor HTTP Client

The old implementation used `java.net.URL` and Gson on a manual `Dispatchers.IO` thread. This was replaced with a Ktor-based `object` singleton using `ContentNegotiation` and `kotlinx.serialization`.

**Key design decisions:**
- `getWeather` is a `suspend` function, so no explicit `Dispatchers.IO` is needed in the ViewModel — Ktor manages its own dispatcher internally.
- Returns `WeatherData?` (nullable), with `null` representing any network or parsing failure — errors are printed but not rethrown, keeping error handling clean in the ViewModel.
- The URL was extended to also request `daily=sunrise,sunset&timezone=auto`, which is needed to correctly determine whether it is currently daytime at the target location.

```kotlin
object WeatherApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
        }
    }

    suspend fun getWeather(lat: Float, lon: Float): WeatherData? {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=$lat&longitude=$lon&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m&")
            append("daily=sunrise,sunset&")
            append("timezone=auto")
        }
        return try { client.get(reqString).body() } catch (e: Exception) { e.printStackTrace(); null }
    }
}
```

**Required Gradle dependencies:**

```kotlin
implementation("io.ktor:ktor-client-core:2.3.12")
implementation("io.ktor:ktor-client-cio:2.3.12")
implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
```

**Required Gradle plugin** (must match your Kotlin version exactly, or `@Serializable` has no effect at compile time):

```kotlin
// app/build.gradle.kts plugins block
kotlin("plugin.serialization") version "2.0.21"

// project/build.gradle.kts plugins block
kotlin("plugin.serialization") version "2.0.21" apply false
```

---

## 2. ViewModel Layer (`viewmodel/`)

### 2.1 `WeatherUIState.kt` — UI State

A single immutable data class holds all state that the UI needs to render. This is the single source of truth, exposed as a `StateFlow` from the ViewModel.

```kotlin
data class SavedLocation(val name: String, val latitude: Float, val longitude: Float)

data class WeatherUIState(
    val latitude: Float = 38.7167f,     // Default: Lisbon
    val longitude: Float = -9.1333f,
    val temperature: Float = 0f,
    val windspeed: Float = 0f,
    val winddirection: Int = 0,
    val weathercode: Int = 0,
    val seaLevelPressure: Float = 0f,
    val time: String = "--",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDay: Boolean = true,
    val savedLocations: List<SavedLocation> = emptyList(),
    val activeLocationName: String? = null,
)
```

`SavedLocation` represents a user-named favourite. `activeLocationName` tracks which favourite (if any) is currently selected, which the UI uses to highlight the active chip.

### 2.2 `WeatherViewModel.kt` — ViewModel

Extends `AndroidViewModel` (rather than plain `ViewModel`) because `fetchFromGPS()` needs an `Application` context to access `LocationManager`.

**Migration from `LiveData` to `StateFlow`:**

The original app used separate `MutableLiveData<WeatherData>` and `MutableLiveData<String>` (for errors). These were replaced with a single `MutableStateFlow<WeatherUIState>`, which is more idiomatic with Jetpack Compose and allows atomic state updates via `.update { it.copy(...) }`.

**`init` block:**

The ViewModel immediately calls `fetchWeather()` on construction, so the UI is never blank even before the GPS permission dialog is answered.

**`fetchWeather()`:**

Replaces the old `WeatherAPI_Call()` which blocked a thread. Now:
1. Sets `isLoading = true` atomically.
2. Launches a coroutine on `viewModelScope` (no `Dispatchers.IO` needed since Ktor is non-blocking).
3. On success: extracts all fields from the `WeatherData` response, calculates the current hour index, determines day/night, and updates state in one `.update` call.
4. On failure (`null` result): sets `errorMessage`.

**`isDay()` logic:**

The original app had a static `companion object` variable `day` on `MainActivity` which was mutated and used to trigger `recreate()`. This is replaced by `isDay` in `WeatherUIState`, computed from the API's `daily.sunrise[0]` and `daily.sunset[0]` fields. The timezone returned by the API is used to correctly interpret current local time at the target location, not the device's timezone.

**`getCurrentHourIndex()`:**

Constructs a timestamp string in the format `"YYYY-MM-DDTHH:00"` using the location's timezone, then finds its index in `hourly.time`. This is used to extract the correct `pressure_msl` value for the current hour (since `current_weather` does not include pressure).

A type mismatch fix was required here: `pressure_msl` is `List<Double>` in the API response, but `seaLevelPressure` in the state is `Float`. The fix is:

```kotlin
seaLevelPressure = result.hourly.pressure_msl.getOrElse(hourIdx) { 0.0 }.toFloat()
```

**`fetchFromGPS()`:**

Annotated with `@RequiresPermission` as required by `LocationManager.getLastKnownLocation()`. Tries `GPS_PROVIDER` first, falls back to `NETWORK_PROVIDER`. If neither has a cached location, uses whatever coordinates are already in state.

**Saved locations methods:**

- `saveLocation(name)` — saves the current coordinates under a name. If a location with the same name already exists, it is replaced (upsert behaviour).
- `selectSavedLocation(loc)` — loads that location's coordinates into state and fetches fresh weather.
- `deleteSavedLocation(loc)` — removes the location; clears `activeLocationName` if the deleted location was active.
- `updateCoordinates(lat, lng)` — convenience method called when the user picks a location on the map, updates both coordinates and immediately fetches weather.

---

## 3. UI Layer (`ui/`)

All composable files live in the `ui` package. The architecture follows Unscramble-style state hoisting: the ViewModel owns state, composables receive it as parameters and emit events via lambda callbacks.

### 3.1 `WeatherCode.kt` — Resource-Based Weather Code Mapping

The original app had a hardcoded enum (`WMO_WeatherCode`) mapping WMO codes to drawable names. This was replaced by a resource-driven approach that reads from `res/values/arrays.xml`, matching the existing data already defined there.

`loadWeatherCodeMap(context)`:
- Looks up the `weather_codes` string-array by name using `getIdentifier()`.
- For each entry name (e.g. `"wc_0"`), loads the corresponding sub-array.
- Each sub-array has 3 items: `[code, description, imageName]`.
- Returns a `Map<Int, WeatherCodeInfo>`.
- Fully guarded against missing or malformed resources — logs errors and continues rather than crashing.
- Wrapped in `remember(context)` in the composable so it only runs once per composition.

`resolveWeatherImageName(code, info, isDay)`:
- For WMO codes 0, 1, 2 (clear/mainly clear/partly cloudy), the image name is a prefix and `"day"` or `"night"` is appended (e.g. `"clear_day"`, `"clear_night"`).
- All other codes use the image name as-is.
- The resource ID is then resolved at runtime via `getIdentifier(..., "drawable", packageName)`.

**Root crash fix:** The original crash (`Resources$NotFoundException: String array resource ID #0x0`) occurred because `getIdentifier()` was called but the result (0 = not found) was passed directly to `getStringArray()`. The fix adds an explicit `if (rootId == 0) return map` guard before any resource access.

### 3.2 `WeatherRow.kt` — Single Info Row

A minimal composable that renders one `label: value` row with `SpaceBetween` alignment.

```kotlin
@Composable
fun WeatherRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface)
    }
}
```

All user-facing strings use `stringResource(R.string.*)` to support localisation. Portuguese translations were added in `res/values-pt/strings.xml`.

### 3.3 `WeatherCard.kt` — Weather Info Card

Renders a Material3 `Card` containing:
1. The weather icon (from drawable resources) and condition label.
2. A `HorizontalDivider`.
3. A `WeatherRow` for each data field: temperature, time, wind speed, wind direction, pressure, latitude, longitude.

The weather icon falls back to a `?` placeholder box if the drawable resource ID resolves to 0 (drawable not found), preventing crashes.

### 3.4 `WeatherScreen.kt` — Root Composable and Layouts

`WeatherScreen` is the top-level composable called from `MainActivity`. It:
1. Collects `uiState` from the ViewModel using `collectAsState()`.
2. Resolves the weather icon resource.
3. Registers an `ActivityResultLauncher` for `LocationPickerActivity` using `rememberLauncherForActivityResult`.
4. Dispatches to `PortraitWeatherScreen` or `LandscapeWeatherScreen` based on `LocalConfiguration.current.orientation`.

**Portrait layout:** Vertical `Column` — title → favourites strip → `WeatherCard` → coordinate inputs.

**Landscape layout:** Horizontal `Row` split into two equal-weight columns — left has title + favourites + `WeatherCard`, right has coordinate inputs.

**`FavoritesStrip`:** A horizontally scrollable `Row` of `InputChip`s. The active location's chip is highlighted via `selected = true`. Each chip has a `✕` trailing button to delete it. Tapping a chip calls `selectSavedLocation()` on the ViewModel.

**`CoordinatesInputSection`:** Contains:
- Latitude `OutlinedTextField` with a `Icons.Default.Public` globe `IconButton` to its right that launches the location picker.
- Longitude `OutlinedTextField`.
- An "Update" `Button` that triggers `fetchWeather()`.
- A "Save Location" `OutlinedButton` that opens a `SaveLocationDialog`.

`Icons.Default.Public` requires the extended Material icons dependency:
```kotlin
implementation("androidx.compose.material:material-icons-extended:1.7.8")
```

**`SaveLocationDialog`:** An `AlertDialog` with a single `OutlinedTextField` for the location name. On confirm, calls `saveLocation(name)` on the ViewModel.

**`ErrorBanner`:** Displays the `errorMessage` from state in a red `errorContainer`-coloured card when non-null.

### 3.5 `LocationPickerActivity.kt` — Map-Based Location Picker

A separate `ComponentActivity` that displays a full-screen Google Map. The user taps anywhere on the map to drop a marker; the selected coordinates are shown in a chip at the bottom of the screen. The top bar has a Cancel button and a ✓ confirm `IconButton`.

On confirm, the coordinates are returned to the caller via `setResult(RESULT_OK, intent)` with `EXTRA_LAT` and `EXTRA_LNG` float extras. On cancel, `RESULT_CANCELED` is returned.

The activity is launched from `WeatherScreen` using `rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())`, which is the modern Compose-compatible replacement for `startActivityForResult`.

**Required dependencies:**
```kotlin
implementation("com.google.android.gms:play-services-maps:19.0.0")
implementation("com.google.maps.android:maps-compose:4.3.3")
```

**Required `AndroidManifest.xml` additions** (inside `<application>`):
```xml
<activity android:name="dam.a51793.coolweatherapp.ui.LocationPickerActivity" />

<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

To obtain an API key:
1. Create a project on [Google Cloud Console](https://console.cloud.google.com).
2. Enable the **Maps SDK for Android** under APIs & Services.
3. Generate an API key and restrict it to your app's package name and SHA-1.

### 3.6 `MainActivity.kt`

Completely rewritten from the XML-based version. Uses `ComponentActivity` instead of `AppCompatActivity`. The ViewModel is obtained via the `by viewModels()` KTX delegate.

GPS permission is requested on launch using `registerForActivityResult(ActivityResultContracts.RequestPermission())`. If granted, `fetchFromGPS()` is called. If denied, the ViewModel's `init {}` block has already fetched weather for the default coordinates (Lisbon), so the UI is never empty.

`setContent` calls `WeatherScreen(weatherViewModel)` inside a `CoolWeatherAppTheme > Surface` hierarchy.

**Required `AndroidManifest.xml` permissions:**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 4. Localisation

All user-facing strings are defined in `res/values/strings.xml` and accessed via `stringResource(R.string.*)` in composables. A Portuguese translation file was added at `res/values-pt/strings.xml`. Android selects the appropriate file automatically based on the device locale — no code changes are required.

Strings defined:

| Key | English | Portuguese |
|---|---|---|
| `current_weather` | Current Weather | Tempo Atual |
| `updateText` | Update | Atualizar |
| `pressureText` | Pressure: | Pressão: |
| `windDir` | Wind Direction: | Direção do Vento: |
| `windSpeed` | Wind Speed: | Velocidade do Vento: |
| `temp` | Temperature: | Temperatura: |
| `time` | Time: | Hora: |
| `latitude` | Latitude: | Latitude: |
| `longitude` | Longitude: | Longitude: |
| `pick_location` | Pick Location | Escolher Localização |
| `save_location` | Save Location | Guardar Localização |
| `location_name` | Location Name | Nome da Localização |
| `confirm` | Confirm | Confirmar |
| `cancel` | Cancel | Cancelar |

---

## 5. File Structure Summary

```
app/src/main/
├── java/dam/a51793/coolweatherapp/
│   ├── data/
│   │   ├── WeatherApiClient.kt     ← Ktor HTTP client (suspend, nullable result)
│   │   └── WeatherData.kt          ← @Serializable data classes
│   ├── viewmodel/
│   │   ├── WeatherUIState.kt       ← UI state + SavedLocation data class
│   │   └── WeatherViewModel.kt     ← StateFlow, fetch, GPS, saved locations
│   ├── ui/
│   │   ├── theme/                  ← Generated theme files (unchanged)
│   │   ├── WeatherCode.kt          ← Resource-based WMO code map + image resolver
│   │   ├── WeatherRow.kt           ← Single label/value row composable
│   │   ├── WeatherCard.kt          ← Icon + all data rows in a Material3 Card
│   │   ├── WeatherScreen.kt        ← Root composable, layouts, favorites, inputs
│   │   ├── WeatherUIState.kt       ← (imported from viewmodel package)
│   │   └── LocationPickerActivity.kt ← Google Maps location picker Activity
│   └── MainActivity.kt             ← ComponentActivity, GPS permission, setContent
└── res/
    ├── values/
    │   ├── strings.xml             ← English strings
    │   ├── arrays.xml              ← WMO weather code data
    │   └── ...
    ├── values-pt/
    │   └── strings.xml             ← Portuguese strings
    └── drawable/
        └── ...                     ← Weather condition drawables
```

---

## 6. Key Bug Fixes and Decisions

| Issue | Cause | Fix |
|---|---|---|
| App crash on launch | `getIdentifier()` returned 0, passed straight to `getStringArray()` | Added `if (rootId == 0) return map` guard |
| `SerializationException` on API call | `@Serializable` only on root class, missing from `Daily`, `CurrentWeather`, `Hourly` | Added `@Serializable` to all nested data classes |
| `Argument type mismatch` on `pressure_msl` | `pressure_msl` is `List<Double>`, state field is `Float` | Added `.toFloat()` after `getOrElse { 0.0 }` |
| UI shown in English on Portuguese device | `values-pt/strings.xml` not created (Translation Editor may not create the file on disk) | Manually created `res/values-pt/strings.xml` with translated strings |
| `Icons.Default.Public` unresolved | Only a subset of icons is bundled by default | Added `material-icons-extended` dependency |
| `@Serializable` has no effect | Kotlin serialization compiler plugin not applied in `build.gradle.kts` | Added `kotlin("plugin.serialization")` to both project and app gradle files |
| `fetchFromGPS` lint warning in `MainActivity` | Method annotated `@RequiresPermission` but called without explicit check visible to lint | Permission is checked immediately before the call; lint satisfied |
