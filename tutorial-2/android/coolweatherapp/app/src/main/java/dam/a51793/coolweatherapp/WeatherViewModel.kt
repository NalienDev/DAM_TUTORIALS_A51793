package dam.a51793.coolweatherapp

import android.app.Application
import android.location.LocationManager
import android.content.Context
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.URL

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Placeholder coordinates (lisbon)
    var currentLat = 38.7167f
    var currentLong = -9.1333f

    fun fetchWeatherData(lat: Float, long: Float) {
        currentLat = lat
        currentLong = long
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = WeatherAPI_Call(lat, long)
                _weatherData.postValue(result)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchFromGPS() {
        val ctx = getApplication<Application>()
        val hasFine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        if (hasFine != PackageManager.PERMISSION_GRANTED) {
            _error.postValue("Location permission not granted")
            return
        }
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            fetchWeatherData(location.latitude.toFloat(), location.longitude.toFloat())
        } else {
            _error.postValue("Could not get GPS location, using default")
            fetchWeatherData(currentLat, currentLong)
        }
    }

    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        val url = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=$lat&longitude=$long&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m&")
            append("daily=sunrise,sunset&")
            append("timezone=auto")
        }
        URL(url).openStream().use {
            return Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
        }
    }

    fun getCurrentHourIndex(weather: WeatherData): Int {
        val tz = java.util.TimeZone.getTimeZone(weather.timezone)
        val cal = java.util.Calendar.getInstance(tz)
        val hour  = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val year  = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) + 1
        val day   = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val target = "%04d-%02d-%02dT%02d:00".format(year, month, day, hour)
        Log.d("WeatherApp", "Looking for time slot: $target in timezone: ${weather.timezone}")
        val idx = weather.hourly.time.indexOf(target)
        Log.d("WeatherApp", "Found index: $idx, time value: ${if (idx != -1) weather.hourly.time[idx] else "NOT FOUND"}")
        return if (idx != -1) idx else hour
    }

    fun isDay(weather: WeatherData): Boolean {
        val tz = java.util.TimeZone.getTimeZone(weather.timezone)
        val cal = java.util.Calendar.getInstance(tz)
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                cal.get(java.util.Calendar.MINUTE)
        val sunrise = weather.daily.sunrise[0].substringAfter("T")
        val sunset  = weather.daily.sunset[0].substringAfter("T")
        fun toMin(t: String): Int { val (h, m) = t.split(":").map { it.toInt() }; return h * 60 + m }
        return nowMinutes in toMin(sunrise)..toMin(sunset)
    }
}