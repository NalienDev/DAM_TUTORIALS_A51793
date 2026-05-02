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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam.a51793.coolweatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherUI(weatherViewModel: WeatherViewModel = viewModel()) {

    val uiState by weatherViewModel.uiState.collectAsState()

    val latitude      = uiState.latitude
    val longitude     = uiState.longitude
    val temperature   = uiState.temperature
    val windSpeed     = uiState.windspeed
    val windDirection = uiState.winddirection
    val weathercode   = uiState.weathercode
    val seaLevelPressure = uiState.seaLevelPressure
    val time          = uiState.time
    val isDay         = uiState.isDay
    val isLoading     = uiState.isLoading
    val errorMessage  = uiState.errorMessage

    val configuration = LocalConfiguration.current
    val context       = LocalContext.current

    val weatherMap = remember(context) { loadWeatherCodeMap(context) }
    val wCodeInfo  = weatherMap[weathercode]

    val wImageName = resolveWeatherImageName(weathercode, wCodeInfo, isDay)

    val wIcon = wImageName?.let {
        context.resources.getIdentifier(it, "drawable", context.packageName)
    } ?: 0

    val weatherLabel = wCodeInfo?.description ?: "Unknown (code $weathercode)"

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeWeatherUI(
            wIcon           = wIcon,
            weatherLabel    = weatherLabel,
            latitude        = latitude,
            longitude       = longitude,
            temperature     = temperature,
            windSpeed       = windSpeed,
            windDirection   = windDirection,
            weathercode     = weathercode,
            seaLevelPressure= seaLevelPressure,
            time            = time,
            isLoading       = isLoading,
            errorMessage    = errorMessage,
            onLatitudeChange  = { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLatitude(it) } },
            onLongitudeChange = { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLongitude(it) } },
            onUpdateButtonClick = { weatherViewModel.fetchWeather() },
        )
    } else {
        PortraitWeatherUI(
            wIcon           = wIcon,
            weatherLabel    = weatherLabel,
            latitude        = latitude,
            longitude       = longitude,
            temperature     = temperature,
            windSpeed       = windSpeed,
            windDirection   = windDirection,
            weathercode     = weathercode,
            seaLevelPressure= seaLevelPressure,
            time            = time,
            isLoading       = isLoading,
            errorMessage    = errorMessage,
            onLatitudeChange  = { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLatitude(it) } },
            onLongitudeChange = { newValue -> newValue.toFloatOrNull()?.let { weatherViewModel.updateLongitude(it) } },
            onUpdateButtonClick = { weatherViewModel.fetchWeather() },
        )
    }
}
@Composable
fun PortraitWeatherUI(
    wIcon: Int,
    weatherLabel: String,
    latitude: Float,
    longitude: Float,
    temperature: Float,
    windSpeed: Float,
    windDirection: Int,
    weathercode: Int,
    seaLevelPressure: Float,
    time: String,
    isLoading: Boolean,
    errorMessage: String?,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(Modifier.height(16.dp))

        // Weather icon + condition label
        WeatherIconSection(wIcon, weatherLabel, iconSize = 120.dp)

        Spacer(Modifier.height(8.dp))

        // Big temperature
        Text(
            text = "%.1f°C".format(temperature),
            fontSize = 64.sp,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = time,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        // Stat cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCard(label = "Wind", value = "%.1f km/h".format(windSpeed), modifier = Modifier.weight(1f))
            StatCard(label = "Direction", value = "${windDirection}°", modifier = Modifier.weight(1f))
            StatCard(label = "Pressure", value = "%.0f hPa".format(seaLevelPressure), modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Coordinates input + update button
        CoordinatesInput(
            latitude          = latitude,
            longitude         = longitude,
            onLatitudeChange  = onLatitudeChange,
            onLongitudeChange = onLongitudeChange,
            onUpdateClick     = onUpdateButtonClick,
            isLoading         = isLoading,
        )

        // Error banner
        errorMessage?.let { ErrorBanner(it) }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun LandscapeWeatherUI(
    wIcon: Int,
    weatherLabel: String,
    latitude: Float,
    longitude: Float,
    temperature: Float,
    windSpeed: Float,
    windDirection: Int,
    weathercode: Int,
    seaLevelPressure: Float,
    time: String,
    isLoading: Boolean,
    errorMessage: String?,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateButtonClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left column: icon + temperature
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            WeatherIconSection(wIcon, weatherLabel, iconSize = 96.dp)

            Text(
                text = "%.1f°C".format(temperature),
                fontSize = 52.sp,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = time,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Right column: stats + inputs
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(label = "Wind",      value = "%.1f km/h".format(windSpeed),         modifier = Modifier.weight(1f))
                StatCard(label = "Direction", value = "${windDirection}°",                    modifier = Modifier.weight(1f))
                StatCard(label = "Pressure",  value = "%.0f hPa".format(seaLevelPressure),   modifier = Modifier.weight(1f))
            }

            CoordinatesInput(
                latitude          = latitude,
                longitude         = longitude,
                onLatitudeChange  = onLatitudeChange,
                onLongitudeChange = onLongitudeChange,
                onUpdateClick     = onUpdateButtonClick,
                isLoading         = isLoading,
            )

            errorMessage?.let { ErrorBanner(it) }
        }
    }
}

@Composable
private fun WeatherIconSection(wIcon: Int, label: String, iconSize: androidx.compose.ui.unit.Dp) {
    if (wIcon != 0) {
        Image(
            painter = painterResource(id = wIcon),
            contentDescription = label,
            modifier = Modifier.size(iconSize),
        )
    } else {
        // Fallback placeholder box when drawable not found
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
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
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
            label = { Text("Latitude") },
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
            label = { Text("Longitude") },
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
                Text("Update weather")
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