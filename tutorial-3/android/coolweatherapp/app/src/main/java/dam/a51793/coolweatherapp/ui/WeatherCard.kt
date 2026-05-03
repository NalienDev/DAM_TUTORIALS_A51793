package dam.a51793.coolweatherapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.a51793.coolweatherapp.R

@Composable
fun WeatherCard(
    wIcon: Int,
    weatherLabel: String,
    uiState: WeatherUIState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WeatherIconSection(wIcon, weatherLabel, iconSize = 96.dp)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            WeatherRow(stringResource(R.string.temp),        "%.1f°C".format(uiState.temperature))
            WeatherRow(stringResource(R.string.time),        uiState.time)
            WeatherRow(stringResource(R.string.windSpeed),   "%.1f km/h".format(uiState.windspeed))
            WeatherRow(stringResource(R.string.windDir),     "${uiState.winddirection}°")
            WeatherRow(stringResource(R.string.pressureText),"%.0f hPa".format(uiState.seaLevelPressure))
            WeatherRow(stringResource(R.string.latitude),    "%.4f".format(uiState.latitude))
            WeatherRow(stringResource(R.string.longitude),   "%.4f".format(uiState.longitude))
        }
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
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("?", fontSize = 32.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(2.dp))
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
