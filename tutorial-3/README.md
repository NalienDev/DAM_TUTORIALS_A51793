# Assignment 3 — Annotation Processing, MVVM with Jetpack Compose, and Multi-Module Architecture

Course: Desenvolvimento de Aplicações Móveis (DAM)  
Student: Lucas Filipe  
Date: May 3, 2026  
Repository URL: https://github.com/NalienDev/DAM_TUTORIALS_A51793

---

## 1. Introduction

This assignment builds on the foundations of Kotlin and Android development established in Assignment 2. The work focuses on three key areas: custom Kotlin annotation processing using KAPT and KotlinPoet, rebuilding the existing WeatherApp with a proper MVVM architecture and Jetpack Compose, and refactoring the Daily Doggo application into a multi-module Android project with a shared core library.

The work is divided into three main components:

- A **Kotlin annotation processing project** (`GreetingProcessorProject`) demonstrating compile-time code generation using custom annotations, `AbstractProcessor`, and KotlinPoet.
- An **Android application** named **CoolWeatherApp**, fully rebuilt using MVVM architecture, Jetpack Compose, Ktor for networking, and `kotlinx.serialization` for JSON parsing.
- An **Android multi-module project** named **DailyDog2**, refactoring the Tutorial 2 Daily Doggo app into a clean three-module architecture (`:core`, `:app-xml`, `:app-compose`).

---

## 2. System Overview

The repository contains three major components:

**Kotlin Console Application (`/kotlin`):** A Gradle multi-project build named `GreetingProcessorProject` demonstrating compile-time annotation processing:
- `annotations/` — defines the custom `@Greeting` and `@Extract` annotations.
- `processor/` — contains `GreetingProcessor` and `RegexProcessor`, which use KAPT + KotlinPoet to generate wrapper and extractor classes at compile time.
- `app/` — the consumer module with `MyClass`, `DataProcessor`, and `Main.kt` that uses the generated code.

**Android Applications (`/android`):**
- **CoolWeatherApp (`coolweatherapp`):** A full MVVM + Compose rebuild of the Tutorial 2 weather app. Fetches real-time climate data from Open-Meteo using Ktor, supports GPS-based and manual coordinate entry, a map-based location picker, saved favourite locations, portrait/landscape adaptive layouts, and Portuguese localisation.
- **DailyDog2 (`dailydog2`):** A multi-module evolution of the Tutorial 2 Daily Doggo app. The shared `:core` module houses all data and business logic (Retrofit, Room, Repository, ViewModel), consumed by two separate application modules — `:app-xml` (the original XML-based UI) and `:app-compose` (a new Jetpack Compose UI with exclusive features).

---

## 3. Architecture and Design

### Kotlin Component — Annotation Processing

The Kotlin project demonstrates the full annotation processing pipeline:

- **Custom Annotations**: `@Greeting(message)` and `@Extract(regex)` are defined with `@Target(AnnotationTarget.FUNCTION)` and `@Retention(AnnotationRetention.SOURCE)`, making them compile-time-only markers that leave no runtime footprint.
- **KAPT Processors**: Both `GreetingProcessor` and `RegexProcessor` extend `AbstractProcessor`. They use `@AutoService(Processor::class)` for automatic service registration, and KotlinPoet's `TypeSpec`, `FunSpec`, and `FileSpec` builders to programmatically construct and write `.kt` source files into the KAPT output directory.
- **Code Generation**: At compile time, `GreetingProcessor` generates a `*Wrapper` class that delegates method calls and prints the greeting message beforehand. `RegexProcessor` generates a concrete `*Extractor` class that subclasses the annotated abstract class and implements each abstract method with a regex-based body.

### Android Component — CoolWeatherApp

- Follows strict **MVVM** architecture with three packages: `data`, `viewmodel`, `ui`.
- The **ViewModel** (`WeatherViewModel`) exposes a single `StateFlow<WeatherUIState>` as the source of truth — replacing the two separate `MutableLiveData` properties from Tutorial 2.
- **Ktor** replaces the manual `java.net.URL` + Gson pattern. `ContentNegotiation` with `kotlinx.serialization` handles automatic JSON deserialization into `@Serializable` data classes.
- All composables follow state hoisting — they receive immutable state and emit events via lambda callbacks.

### Android Component — DailyDog2 Multi-Module

- Three separated Gradle modules with clear dependency flow: `:app-xml` → `:core` ← `:app-compose`.
- `:core` is an Android Library module exposing Room, Retrofit, Repository, and ViewModel as `api()` dependencies so both application modules can resolve the shared types.
- `:app-compose` introduces Compose-exclusive features absent from `:app-xml`: `LazyVerticalGrid` with adaptive columns, dynamic Light/Dark theming via `isSystemInDarkTheme()`, and `animateContentSize` micro-animations on the favourite icon.

---

## 4. Implementation

### Kotlin — Annotation Processing (`GreetingProcessorProject`)

**`annotations/Greeting.kt` and `annotations/Extract.kt`**

Two source-retained custom annotations targeting functions:
- `@Greeting(val message: String)` — marks methods that should receive a greeting print before execution.
- `@Extract(val regex: String)` — marks abstract methods that should be implemented with regex-based string extraction.

**`processor/GreetingProcessor.kt` — Wrapper Class Generator**

Processes all methods annotated with `@Greeting` at compile time:
- Groups annotated `ExecutableElement`s by their enclosing class.
- For each class, generates a `${OriginalClassName}Wrapper` using KotlinPoet's `TypeSpec.classBuilder()`.
- The wrapper holds an `original` reference via composition and, for each annotated method, generates a delegating function that first calls `println(greetingMessage)` then delegates to `original.methodName(args)`.
- The generated file is written to the `kapt.kotlin.generated` directory using `FileSpec.writeTo()`.

**`processor/RegexProcessor.kt` — Extractor Class Generator**

Processes all methods annotated with `@Extract`:
- Generates a `${OriginalClassName}Extractor` class that extends the abstract class via inheritance.
- Each overriding method body uses `Regex(pattern).find(input)` and returns `match?.groupValues?.get(1)` (capture group 1), returning `String?`.

**`app/MyClass.kt` and `app/DataProcessor.kt` — Consumer Code**

- `MyClass` annotates its `sayHello()` and `compute()` methods with `@Greeting`, triggering the generation of `MyClassWrapper`.
- `DataProcessor` is an abstract class with `getName()` and `getAddress()` annotated with `@Extract`, triggering the generation of `DataProcessorExtractor`.
- `Main.kt` uses both generated classes, validating the full annotation processing pipeline end-to-end.

```kotlin
fun main() {
    val myClass = MyClass()
    val wrapped = MyClassWrapper(myClass)
    wrapped.sayHello()   // prints greeting, then delegates

    val extractor = DataProcessorExtractor("Name:John Address:123 Street")
    println("Name: ${extractor.getName()}")       // → John
    println("Address: ${extractor.getAddress()}") // → 123 Street
}
```

---

### Android — CoolWeatherApp

For full implementation details, refer to [`tutorial-3/android/coolweatherapp/README.md`](android/coolweatherapp/README.md). Key highlights:

**Data Layer (`data/`)**
- `WeatherData.kt` — all data classes annotated with `@Serializable`. `ArrayList<T>` replaced with `List<T>`. All fields changed from `var` to `val`.
- `WeatherApiClient.kt` — Ktor `HttpClient` object with `ContentNegotiation` plugin. `getWeather(lat, lon)` is a `suspend` function returning `WeatherData?`. URL extended to also request `daily=sunrise,sunset&timezone=auto` for accurate day/night determination at the target location.

**ViewModel Layer (`viewmodel/`)**
- `WeatherUIState.kt` — single immutable data class holding all UI state: coordinates, weather metrics, loading/error flags, `isDay` boolean, and list of `SavedLocation` favourites.
- `WeatherViewModel.kt` — exposes `StateFlow<WeatherUIState>`. Replaces `LiveData` with atomic `.update { it.copy(...) }` calls. Determines day/night using the API's sunrise/sunset times in the target timezone. Supports GPS location, manual coordinates, and named saved locations.

**UI Layer (`ui/`)**
- `WeatherScreen.kt` — root composable, dispatches to `PortraitWeatherScreen` or `LandscapeWeatherScreen` based on `LocalConfiguration.current.orientation`.
- `WeatherCard.kt` — Material3 Card with weather icon and `WeatherRow` entries for all fields.
- `LocationPickerActivity.kt` — separate `ComponentActivity` with a full-screen Google Map; returns coordinates via `setResult`.
- Portuguese localisation via `res/values-pt/strings.xml`.

---

### Android — DailyDog2 Multi-Module Architecture

For full implementation details, refer to [`tutorial-3/android/dailydog2/README.md`](android/dailydog2/README.md). Key highlights:

**Module Structure**

```
dailydog2/
├── core/           ← Android Library: api, data, repository, viewmodel
├── app-xml/        ← Android App: XML layouts, RecyclerView, Activities
└── app-compose/    ← Android App: Full Jetpack Compose UI
```

**`:core` Module**
- Contains `DogApiService` (Retrofit), `RetrofitInstance`, `DogItem`, `DogDao`, `DogDatabase` (Room), `DogRepository`, and `DogViewModel`.
- Room and ViewModel dependencies declared as `api()` so consumers can resolve Room supertypes (`RoomDatabase`, etc.) without redeclaring them.

**`:app-xml` Module**
- Refactored version of Tutorial 2's Daily Doggo app.
- Depends on `:core` via `implementation(project(":core"))`.
- UI unchanged: `MainActivity` with `SwipeRefreshLayout` + `RecyclerView` grid, `DetailsActivity` with Glide image loading and FAB favourite toggle.

**`:app-compose` Module**
- New module with Jetpack Compose, Material3, and Coil.
- `DogListScreen` uses `LazyVerticalGrid(GridCells.Adaptive(150.dp))` — adapts column count to screen width automatically.
- `DogDetailScreen` displays the full-screen image and an animated favourite icon.
- `DailyDogTheme` wraps the app in a `MaterialTheme` with explicit dark and light `ColorScheme` objects driven by `isSystemInDarkTheme()`.
- Favourite button uses `animateContentSize` with a `spring` animation spec and a dynamic icon size (`36.dp` when active, `24.dp` when inactive) for fluid visual feedback.

**Compose-Exclusive Features Summary**

| Feature | `:app-xml` | `:app-compose` |
|---|---|---|
| Grid layout | `RecyclerView` + `GridLayoutManager` | `LazyVerticalGrid` (Adaptive) |
| Dark mode | System theme only | `MaterialTheme` + `isSystemInDarkTheme()` |
| Favourite animation | None | `animateContentSize` + icon size transition |
| Navigation | Intent + `startActivity` | In-composition state (`selectedDog`) |

---

## 5. Testing and Validation

**Kotlin Annotation Processing:**
- Validated by running `Main.kt` after a clean build — the generated `MyClassWrapper` and `DataProcessorExtractor` files appear in the KAPT output directory (`build/generated/source/kaptKotlin/`).
- Confirmed that `MyClassWrapper.sayHello()` prints the greeting before delegating, and that `DataProcessorExtractor.getName()` correctly extracts `"John"` from the input string.

**CoolWeatherApp:**
- GPS permission denial handled gracefully — ViewModel's `init` block fetches weather for default Lisbon coordinates before the permission dialog resolves.
- Day/night background computed from target-location timezone, not device timezone — validated by entering coordinates for locations in different UTC offsets.
- Portrait and landscape layouts verified on emulator with screen rotation.
- Portuguese locale strings tested by switching device language to Portuguese.

**DailyDog2:**
- Clean build confirms all three modules compile: `:core:assembleDebug` ✅, `:app-xml:assembleDebug` ✅, `:app-compose:assembleDebug` ✅.
- Room supertype resolution issue (`unresolved supertypes: RoomDatabase`) fixed by changing Room and ViewModel to `api()` in `:core/build.gradle.kts`.

---

## 6. Usage Instructions

### Kotlin — GreetingProcessorProject

1. Open **IntelliJ IDEA**.
2. Select `Open` and choose `tutorial-3/kotlin/GreetingProcessorProject`.
3. Wait for Gradle sync to complete.
4. Run `Main.kt` via the green play button next to the `main()` function.
5. Inspect generated code in `app/build/generated/source/kaptKotlin/main/app/`.

### Android — CoolWeatherApp

1. Open **Android Studio**.
2. Select `File > Open` and navigate to `tutorial-3/android/coolweatherapp`.
3. Wait for **Gradle Sync** to finish.
4. Add your Google Maps API key to `AndroidManifest.xml` inside the `<meta-data>` tag for the location picker.
5. Select emulator/device and run with **Shift + F10**.

### Android — DailyDog2

1. Open **Android Studio**.
2. Select `File > Open` and navigate to `tutorial-3/android/dailydog2`.
3. Wait for **Gradle Sync** to finish.
4. Select the desired run configuration (`:app-xml` or `:app-compose`) from the dropdown.
5. Run with **Shift + F10**.

---

# Autonomous Software Engineering Sections

## 7. Prompting Strategy

AI assistance (Antigravity) was used for the **DailyDog2** multi-module refactoring. The prompting strategy followed a planning-first approach: the architecture was agreed upon before any code was written, including the module dependency graph, what would live in `:core`, and what Compose-exclusive features would differentiate `:app-compose`.

Example constraint prompt:
> *"Refactor the DailyDog app into a multi-module project with three modules: :core (shared data/business logic), :app-xml (refactored original UI), and :app-compose (new Compose UI). The Compose app must have at least one exclusive feature not present in the XML version."*

For CoolWeatherApp and the Kotlin annotation processing project, the implementation was human-authored to reinforce understanding of MVVM state management, Jetpack Compose, and Kotlin metaprogramming.

---

## 8. Autonomous Agent Workflow

For DailyDog2, the agent managed the full refactoring:
- **Module Setup**: Created `settings.gradle.kts` with three module includes, `core/build.gradle.kts` with Android Library plugin, and `app-compose/build.gradle.kts` with Compose configuration.
- **File Migration**: Moved `api/`, `data/`, `repository/`, and `viewmodel/` packages from `:app-xml` to `:core` using filesystem operations.
- **Dependency Resolution**: Identified and fixed the `unresolved supertypes: RoomDatabase` error by changing Room and ViewModel from `implementation` to `api` in `:core`.
- **Compose UI**: Implemented the full `MainActivity.kt` for `:app-compose` including `DailyDogTheme`, `DogListScreen`, `DogCard`, and `DogDetailScreen` composables with animations and adaptive grid.

---

## 9. Verification of AI-Generated Artifacts

- **Build verification**: All three modules built successfully (`BUILD SUCCESSFUL`) after resolving the Room supertype exposure issue.
- **Code review**: The generated Compose UI was reviewed to confirm correct usage of `observeAsState`, `LaunchedEffect`, `rememberCoroutineScope`, and `animateContentSize`.
- **Import correctness**: Deprecated `Icons.Filled.ArrowBack` was identified and replaced with `Icons.AutoMirrored.Filled.ArrowBack`.

---

## 10. Human vs AI Contribution

| Component | Primary Author |
|---|---|
| Kotlin: `@Greeting` and `@Extract` annotations | Human |
| Kotlin: `GreetingProcessor` (KotlinPoet wrapper generation) | Human |
| Kotlin: `RegexProcessor` (KotlinPoet extractor generation) | Human |
| Android: CoolWeatherApp — Data layer (Ktor, `@Serializable`) | Human |
| Android: CoolWeatherApp — ViewModel (`StateFlow`, GPS, favourites) | Human |
| Android: CoolWeatherApp — UI (Compose, adaptive layouts, map picker) | Human |
| Android: DailyDog2 — Module structure and Gradle setup | AI (Antigravity) |
| Android: DailyDog2 — `:core` extraction and `:app-xml` refactor | AI (Antigravity) |
| Android: DailyDog2 — `:app-compose` Compose UI | AI (Antigravity) |
| Android: DailyDog2 — Bug fixes and dependency resolution | Human + AI |
| Documentation | Human (+ AI review context) |

---

## 11. Ethical and Responsible Use

- **Understanding First**: AI was used exclusively for the DailyDog2 refactoring, where the structural transformation was mechanical (moving files, updating imports, wiring Compose). The logic in `:core` was pre-existing and understood from Tutorial 2.
- **Independence**: The annotation processing project and CoolWeatherApp rebuild were kept entirely human-authored to develop mastery over compile-time metaprogramming and Jetpack Compose state management.
- All AI-generated code was reviewed, tested, and understood before submission.

---

# Development Process

## 12. Version Control and Commit History

All work was committed progressively to the Git repository. The multi-module DailyDog2 project was committed in a single structured commit after all three modules built successfully. CoolWeatherApp changes were committed alongside, reflecting the iterative development of MVVM + Compose features.

## 13. Difficulties and Lessons Learned



## 14. Future Improvements

- **DailyDog2**: Extract `DogViewModel` state to `StateFlow` instead of `LiveData` to make it more idiomatic with Jetpack Compose and eliminate the need for the `runtime-livedata` bridge dependency in `:app-compose`.
- **CoolWeatherApp**: Add offline caching of the last fetched weather data using Room or DataStore, so the app can display the last known weather when offline instead of showing an error.
- **DailyDog2 `:app-compose`**: Add `pull-to-refresh` using Compose's `PullRefreshIndicator` from Material3 experimental APIs, providing gesture-based refresh parity with the SwipeRefreshLayout in `:app-xml`.

## 15. AI Usage Disclosure

| Tool | How it was used |
|---|---|
| **Antigravity** | Planned and executed the DailyDog2 multi-module refactoring: Gradle setup, file migration, Compose UI implementation, and build error resolution. |
| **Claude / ChatGPT** | Consulted for specific questions about KotlinPoet API usage and KAPT processor registration with `@AutoService`. |

I confirm responsibility for the artifact contents produced utilizing generative tools where listed, fully understanding all codebase logic utilized structurally.
