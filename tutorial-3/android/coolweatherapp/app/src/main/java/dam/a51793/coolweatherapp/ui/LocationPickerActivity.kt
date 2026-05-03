package dam.a51793.coolweatherapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dam.a51793.coolweatherapp.R
import dam.a51793.coolweatherapp.ui.theme.CoolWeatherAppTheme

class LocationPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read optional starting coords passed from the caller
        val startLat = intent.getFloatExtra(EXTRA_LAT, 38.7167f).toDouble()
        val startLng = intent.getFloatExtra(EXTRA_LNG, -9.1333f).toDouble()

        setContent {
            CoolWeatherAppTheme {
                LocationPickerScreen(
                    startLat = startLat,
                    startLng = startLng,
                    onConfirm = { lat, lng ->
                        val result = Intent().apply {
                            putExtra(EXTRA_LAT, lat.toFloat())
                            putExtra(EXTRA_LNG, lng.toFloat())
                        }
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickerScreen(
    startLat: Double,
    startLng: Double,
    onConfirm: (Double, Double) -> Unit,
    onCancel: () -> Unit,
) {
    var pickedLatLng by remember { mutableStateOf(LatLng(startLat, startLng)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(startLat, startLng), 5f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pick_location)) },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onConfirm(pickedLatLng.latitude, pickedLatLng.longitude)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    pickedLatLng = latLng
                }
            ) {
                Marker(
                    state = MarkerState(position = pickedLatLng),
                    title = "%.4f, %.4f".format(pickedLatLng.latitude, pickedLatLng.longitude)
                )
            }

            // Coordinates chip at the bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 4.dp,
            ) {
                Text(
                    text = "%.4f,  %.4f".format(pickedLatLng.latitude, pickedLatLng.longitude),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}
