package dam.a51793.coolweatherapp.ui

import android.content.res.Configuration
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

    val onLatitudeChange:  (String) -> Unit = { v -> v.toFloatOrNull()?.let { weatherViewModel.updateLatitude(it) } }
    val onLongitudeChange: (String) -> Unit = { v -> v.toFloatOrNull()?.let { weatherViewModel.updateLongitude(it) } }
    val onUpdateClick = { weatherViewModel.fetchWeather() }

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeWeatherScreen(
            wIcon             = wIcon,
            weatherLabel      = weatherLabel,
            uiState           = uiState,
            onLatitudeChange  = onLatitudeChange,
            onLongitudeChange = onLongitudeChange,
            onUpdateClick     = onUpdateClick,
        )
    } else {
        PortraitWeatherScreen(
            wIcon             = wIcon,
            weatherLabel      = weatherLabel,
            uiState           = uiState,
            onLatitudeChange  = onLatitudeChange,
            onLongitudeChange = onLongitudeChange,
            onUpdateClick     = onUpdateClick,
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

        WeatherCard(
            wIcon        = wIcon,
            weatherLabel = weatherLabel,
            uiState      = uiState,
        )

        CoordinatesInputSection(
            latitude          = uiState.latitude,
            longitude         = uiState.longitude,
            onLatitudeChange  = onLatitudeChange,
            onLongitudeChange = onLongitudeChange,
            onUpdateClick     = onUpdateClick,
            isLoading         = uiState.isLoading,
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
            WeatherCard(
                wIcon        = wIcon,
                weatherLabel = weatherLabel,
                uiState      = uiState,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoordinatesInputSection(
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
private fun CoordinatesInputSection(
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
    Spacer(Modifier.height(4.dp))
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
