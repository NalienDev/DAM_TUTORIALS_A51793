package dam.a51793.coolweatherapp.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam.a51793.coolweatherapp.R
import dam.a51793.coolweatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherUI(weatherViewModel: WeatherViewModel = viewModel()) {

    val uiState          by weatherViewModel.uiState.collectAsState()
    val configuration    = LocalConfiguration.current
    val context          = LocalContext.current

    val weatherMap  = remember(context) { loadWeatherCodeMap(context) }
    val wCodeInfo   = weatherMap[uiState.weathercode]
    val wImageName  = resolveWeatherImageName(uiState.weathercode, wCodeInfo, uiState.isDay)
    val wIcon       = wImageName?.let {
        context.resources.getIdentifier(it, "drawable", context.packageName)
    } ?: 0
    val weatherLabel = wCodeInfo?.description ?: "Unknown (code ${uiState.weathercode})"

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeWeatherUI(
            wIcon            = wIcon,
            weatherLabel     = weatherLabel,
            uiState          = uiState,
            onLatitudeChange = { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLatitude(it) } },
            onLongitudeChange= { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLongitude(it) } },
            onUpdateClick    = { weatherViewModel.fetchWeather() },
        )
    } else {
        PortraitWeatherUI(
            wIcon            = wIcon,
            weatherLabel     = weatherLabel,
            uiState          = uiState,
            onLatitudeChange = { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLatitude(it) } },
            onLongitudeChange= { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLongitude(it) } },
            onUpdateClick    = { weatherViewModel.fetchWeather() },
        )
    }
}

@Composable
fun PortraitWeatherUI(
    wIcon: Int,
    weatherLabel: String,
    uiState: WeatherUIState,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.current_weather),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(12.dp))

        WeatherIconSection(wIcon, weatherLabel, iconSize = 120.dp)

        Spacer(Modifier.height(16.dp))

        WeatherInfoBlock(uiState)

        Spacer(Modifier.height(24.dp))

        CoordinatesInput(
            latitude          = uiState.latitude,
            longitude         = uiState.longitude,
            onLatitudeChange  = onLatitudeChange,
            onLongitudeChange = onLongitudeChange,
            onUpdateClick     = onUpdateClick,
            isLoading         = uiState.isLoading,
        )

        uiState.errorMessage?.let { ErrorBanner(it) }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun LandscapeWeatherUI(
    wIcon: Int,
    weatherLabel: String,
    uiState: WeatherUIState,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: icon + info
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.current_weather),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            WeatherIconSection(wIcon, weatherLabel, iconSize = 96.dp)
            Spacer(Modifier.height(12.dp))
            WeatherInfoBlock(uiState)
        }

        // Right: coordinates input
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoordinatesInput(
                latitude          = uiState.latitude,
                longitude         = uiState.longitude,
                onLatitudeChange  = onLatitudeChange,
                onLongitudeChange = onLongitudeChange,
                onUpdateClick     = onUpdateClick,
                isLoading         = uiState.isLoading,
            )
            uiState.errorMessage?.let { ErrorBanner(it) }
        }
    }
}

@Composable
private fun WeatherInfoBlock(uiState: WeatherUIState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoRow(stringResource(R.string.temp),      "%.1f°C".format(uiState.temperature))
            InfoRow(stringResource(R.string.time),      uiState.time)
            InfoRow(stringResource(R.string.windSpeed), "%.1f km/h".format(uiState.windspeed))
            InfoRow(stringResource(R.string.windDir),   "${uiState.winddirection}°")
            InfoRow(stringResource(R.string.pressureText), "%.0f hPa".format(uiState.seaLevelPressure))
            InfoRow(stringResource(R.string.latitude),  "%.4f".format(uiState.latitude))
            InfoRow(stringResource(R.string.longitude), "%.4f".format(uiState.longitude))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WeatherIconSection(wIcon: Int, label: String, iconSize: Dp) {
    if (wIcon != 0) {
        Image(
            painter = painterResource(id = wIcon),
            contentDescription = label,
            modifier = Modifier.size(iconSize),
        )
    } else {
        Box(
            modifier = Modifier
                .size(iconSize)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("?", fontSize = 32.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun CoordinatesInput(
    latitude: Float,
    longitude: Float,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
    isLoading: Boolean,
) {
    var latText  by remember(latitude)  { mutableStateOf(latitude.toString()) }
    var longText by remember(longitude) { mutableStateOf(longitude.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = latText,
            onValueChange = { latText = it; onLatitudeChange(it) },
            label = { Text(stringResource(R.string.latitude)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
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
        Button(
            onClick = onUpdateClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Spacer(Modifier.height(8.dp))
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