package dam.a51793.coolweatherapp.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dam.a51793.coolweatherapp.data.WeatherApiClient
import dam.a51793.coolweatherapp.data.WeatherData
import dam.a51793.coolweatherapp.ui.WeatherUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WeatherUIState())
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    init {
        fetchWeather()
    }

    fun updateLatitude(lat: Float) {
        _uiState.update { it.copy(latitude = lat) }
    }

    fun updateLongitude(long: Float) {
        _uiState.update { it.copy(longitude = long) }
    }

    fun fetchWeather() {
        val lat  = _uiState.value.latitude
        val long = _uiState.value.longitude
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = WeatherApiClient.getWeather(lat, long)
            if (result == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to fetch weather data") }
                return@launch
            }
            val hourIdx = getCurrentHourIndex(result)
            val isDay   = isDay(result)
            _uiState.update {
                it.copy(
                    isLoading        = false,
                    latitude         = result.latitude.toFloatOrNull()  ?: lat,
                    longitude        = result.longitude.toFloatOrNull() ?: long,
                    temperature      = result.current_weather.temperature,
                    windspeed        = result.current_weather.windspeed,
                    winddirection    = result.current_weather.winddirection,
                    weathercode      = result.current_weather.weathercode,
                    seaLevelPressure = result.hourly.pressure_msl.getOrElse(hourIdx) { 0.0 }.toFloat(),
                    time             = result.current_weather.time,
                    isDay            = isDay,
                )
            }
        }
    }

    fun fetchFromGPS() {
        val ctx    = getApplication<Application>()
        val hasFine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        if (hasFine != PackageManager.PERMISSION_GRANTED) {
            _uiState.update { it.copy(errorMessage = "Location permission not granted") }
            return
        }
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            _uiState.update {
                it.copy(
                    latitude  = location.latitude.toFloat(),
                    longitude = location.longitude.toFloat(),
                )
            }
        } else {
            _uiState.update { it.copy(errorMessage = "Could not get GPS location, using default") }
        }
        fetchWeather()
    }

    private fun getCurrentHourIndex(weather: WeatherData): Int {
        val tz  = java.util.TimeZone.getTimeZone(weather.timezone)
        val cal = java.util.Calendar.getInstance(tz)
        val target = "%04d-%02d-%02dT%02d:00".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
            cal.get(java.util.Calendar.HOUR_OF_DAY),
        )
        Log.d("WeatherApp", "Looking for time slot: $target in timezone: ${weather.timezone}")
        val idx = weather.hourly.time.indexOf(target)
        Log.d("WeatherApp", "Found index: $idx")
        return if (idx != -1) idx else cal.get(java.util.Calendar.HOUR_OF_DAY)
    }

    private fun isDay(weather: WeatherData): Boolean {
        val sunrise = weather.daily.sunrise.getOrNull(0)?.substringAfter("T") ?: return true
        val sunset  = weather.daily.sunset.getOrNull(0)?.substringAfter("T")  ?: return true
        val tz  = java.util.TimeZone.getTimeZone(weather.timezone)
        val cal = java.util.Calendar.getInstance(tz)
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                cal.get(java.util.Calendar.MINUTE)
        fun toMin(t: String): Int {
            val (h, m) = t.split(":").map { it.toInt() }
            return h * 60 + m
        }
        return nowMinutes in toMin(sunrise)..toMin(sunset)
    }
}