package dam.a51793.coolweatherapp.ui

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam.a51793.coolweatherapp.R
import dam.a51793.coolweatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel = viewModel()) {

    val uiState       by weatherViewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val context       = LocalContext.current

    val weatherMap   = remember(context) { loadWeatherCodeMap(context) }
    val wCodeInfo    = weatherMap[uiState.weathercode]
    val wImageName   = resolveWeatherImageName(uiState.weathercode, wCodeInfo, uiState.isDay)
    val wIcon        = wImageName?.let {
        context.resources.getIdentifier(it, "drawable", context.packageName)
    } ?: 0
    val weatherLabel = wCodeInfo?.description ?: "Unknown (code ${uiState.weathercode})"

    // Launcher for LocationPickerActivity
    val locationPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getFloatExtra(LocationPickerActivity.EXTRA_LAT, 0f) ?: return@rememberLauncherForActivityResult
            val lng = result.data?.getFloatExtra(LocationPickerActivity.EXTRA_LNG, 0f) ?: return@rememberLauncherForActivityResult
            weatherViewModel.updateCoordinates(lat, lng)
        }
    }

    val onOpenLocationPicker = {
        val intent = Intent(context, LocationPickerActivity::class.java).apply {
            putExtra(LocationPickerActivity.EXTRA_LAT, uiState.latitude)
            putExtra(LocationPickerActivity.EXTRA_LNG, uiState.longitude)
        }
        locationPickerLauncher.launch(intent)
    }

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeWeatherScreen(
            wIcon               = wIcon,
            weatherLabel        = weatherLabel,
            uiState             = uiState,
            onLatitudeChange    = { v -> v.toFloatOrNull()?.let { weatherViewModel.updateLatitude(it) } },
            onLongitudeChange   = { v -> v.toFloatOrNull()?.let { weatherViewModel.updateLongitude(it) } },
            onUpdateClick       = { weatherViewModel.fetchWeather() },
            onOpenLocationPicker= onOpenLocationPicker,
            onSaveLocation      = { name -> weatherViewModel.saveLocation(name) },
            onSelectLocation    = { loc -> weatherViewModel.selectSavedLocation(loc) },
            onDeleteLocation    = { loc -> weatherViewModel.deleteSavedLocation(loc) },
        )
    } else {
        PortraitWeatherScreen(
            wIcon               = wIcon,
            weatherLabel        = weatherLabel,
            uiState             = uiState,
            onLatitudeChange    = { v -> v.toFloatOrNull()?.let { weatherViewModel.updateLatitude(it) } },
            onLongitudeChange   = { v -> v.toFloatOrNull()?.let { weatherViewModel.updateLongitude(it) } },
            onUpdateClick       = { weatherViewModel.fetchWeather() },
            onOpenLocationPicker= onOpenLocationPicker,
            onSaveLocation      = { name -> weatherViewModel.saveLocation(name) },
            onSelectLocation    = { loc -> weatherViewModel.selectSavedLocation(loc) },
            onDeleteLocation    = { loc -> weatherViewModel.deleteSavedLocation(loc) },
        )
    }
}

@Composable
fun PortraitWeatherScreen(
    wIcon: Int,
    weatherLabel: String,
    uiState: WeatherUIState,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onSaveLocation: (String) -> Unit,
    onSelectLocation: (SavedLocation) -> Unit,
    onDeleteLocation: (SavedLocation) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.current_weather),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        // Favorites strip
        if (uiState.savedLocations.isNotEmpty()) {
            FavoritesStrip(
                locations      = uiState.savedLocations,
                activeName     = uiState.activeLocationName,
                onSelect       = onSelectLocation,
                onDelete       = onDeleteLocation,
            )
        }

        WeatherCard(wIcon = wIcon, weatherLabel = weatherLabel, uiState = uiState)

        CoordinatesInputSection(
            latitude             = uiState.latitude,
            longitude            = uiState.longitude,
            onLatitudeChange     = onLatitudeChange,
            onLongitudeChange    = onLongitudeChange,
            onUpdateClick        = onUpdateClick,
            onOpenLocationPicker = onOpenLocationPicker,
            onSaveLocation       = onSaveLocation,
            isLoading            = uiState.isLoading,
        )

        uiState.errorMessage?.let { ErrorBanner(it) }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun LandscapeWeatherScreen(
    wIcon: Int,
    weatherLabel: String,
    uiState: WeatherUIState,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onSaveLocation: (String) -> Unit,
    onSelectLocation: (SavedLocation) -> Unit,
    onDeleteLocation: (SavedLocation) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.current_weather),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (uiState.savedLocations.isNotEmpty()) {
                FavoritesStrip(
                    locations  = uiState.savedLocations,
                    activeName = uiState.activeLocationName,
                    onSelect   = onSelectLocation,
                    onDelete   = onDeleteLocation,
                )
            }
            WeatherCard(wIcon = wIcon, weatherLabel = weatherLabel, uiState = uiState)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoordinatesInputSection(
                latitude             = uiState.latitude,
                longitude            = uiState.longitude,
                onLatitudeChange     = onLatitudeChange,
                onLongitudeChange    = onLongitudeChange,
                onUpdateClick        = onUpdateClick,
                onOpenLocationPicker = onOpenLocationPicker,
                onSaveLocation       = onSaveLocation,
                isLoading            = uiState.isLoading,
            )
            uiState.errorMessage?.let { ErrorBanner(it) }
        }
    }
}

@Composable
private fun FavoritesStrip(
    locations: List<SavedLocation>,
    activeName: String?,
    onSelect: (SavedLocation) -> Unit,
    onDelete: (SavedLocation) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        locations.forEach { loc ->
            val isActive = loc.name == activeName
            InputChip(
                selected = isActive,
                onClick  = { onSelect(loc) },
                label    = { Text(loc.name) },
                trailingIcon = {
                    TextButton(
                        onClick = { onDelete(loc) },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(20.dp),
                    ) {
                        Text("✕", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
    }
}

@Composable
private fun CoordinatesInputSection(
    latitude: Float,
    longitude: Float,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onSaveLocation: (String) -> Unit,
    isLoading: Boolean,
) {
    var latText      by remember(latitude)  { mutableStateOf(latitude.toString()) }
    var longText     by remember(longitude) { mutableStateOf(longitude.toString()) }
    var locationName by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = latText,
                onValueChange = { latText = it; onLatitudeChange(it) },
                label = { Text(stringResource(R.string.latitude)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenLocationPicker) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = stringResource(R.string.pick_location),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        OutlinedTextField(
            value = longText,
            onValueChange = { longText = it; onLongitudeChange(it) },
            label = { Text(stringResource(R.string.longitude)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onUpdateClick() }),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onUpdateClick,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Loading…")
                } else {
                    Text(stringResource(R.string.updateText))
                }
            }

            OutlinedButton(
                onClick = { showSaveDialog = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.save_location))
            }
        }
    }

    if (showSaveDialog) {
        SaveLocationDialog(
            initialName = locationName,
            onConfirm = { name ->
                locationName = name
                onSaveLocation(name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false },
        )
    }
}

@Composable
private fun SaveLocationDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(stringResource(R.string.save_location)) },
        text    = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.location_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}