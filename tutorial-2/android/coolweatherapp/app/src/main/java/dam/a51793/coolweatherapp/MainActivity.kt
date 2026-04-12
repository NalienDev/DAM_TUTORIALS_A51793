package dam.a51793.coolweatherapp

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    var day = true  // this is determined by the current time and sunrise/sunset hours
    private lateinit var latInput: EditText
    private lateinit var longInput: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (day) {
                    setTheme(R.style.Theme_Day)
                } else {
                    setTheme(R.style.Theme_Night)
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) {
                    setTheme(R.style.Theme_Day_Land)
                } else {
                    setTheme(R.style.Theme_Night_Land)
                }
            }
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        latInput  = findViewById(R.id.latView)
        longInput = findViewById(R.id.longView)
        latInput.setText("38.7167")
        longInput.setText("-9.1333")

        fetchWeatherData(38.7167f, -9.1333f)

        findViewById<Button>(R.id.updateBtn).setOnClickListener {
            val lat  = latInput.text.toString().toFloatOrNull()
            val long = longInput.text.toString().toFloatOrNull()

            if (lat == null || long == null) {
                Log.e("WeatherAPI", "Invalid coordinates entered")
                Toast.makeText(this, "Enter valid coordinates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchWeatherData(lat, long)
        }
    }

    private fun getCurrentHourIndex(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }

    private fun isDay(weather: WeatherData): Boolean {
        val sunrise = weather.daily.sunrise[0].substringAfter("T")
        val sunset  = weather.daily.sunset[0].substringAfter("T")

        val cal = java.util.Calendar.getInstance()
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                cal.get(java.util.Calendar.MINUTE)

        fun timeToMinutes(t: String): Int {
            val (h, m) = t.split(":").map { it.toInt() }
            return h * 60 + m
        }

        return nowMinutes in timeToMinutes(sunrise)..timeToMinutes(sunset)
    }

    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=$lat&longitude=$long&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m&")
            append("daily=sunrise,sunset&")
            append("timezone=auto")
        }

        val url = URL(reqString)

        url.openStream().use {
            return Gson().fromJson(
                InputStreamReader(it, "UTF-8"),
                WeatherData::class.java
            )
        }
    }

    private fun fetchWeatherData(lat: Float, long: Float): Thread {
        return Thread {
            try {
                val weather = WeatherAPI_Call(lat, long)
                updateUI(weather)
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Failed to fetch weather: ${e.message}")
            }
        }.also { it.start() }
    }

    private fun updateUI(request: WeatherData) {
        runOnUiThread {
            val weatherImage: ImageView = findViewById(R.id.weatherImage)
            val pressure: TextView = findViewById(R.id.pressureView)
            val windDir: TextView = findViewById(R.id.windDirView)
            val windSpeed: TextView = findViewById(R.id.windSpeedView)
            val temperature: TextView = findViewById(R.id.temperatureView)
            val time: TextView = findViewById(R.id.timeView)

            val idx = getCurrentHourIndex()
            day = isDay(request)

            pressure.text = request.hourly.pressure_msl[idx].toString() + " hPa"
            temperature.text = request.hourly.temperature_2m[idx].toString() + "°C"
            time.text = request.hourly.time[idx]
            windDir.text = request.current_weather.winddirection.toString()
            windSpeed.text = request.current_weather.windspeed.toString()

            val mapt = getWeatherCodeMap()
            val wCode = mapt[request.current_weather.weathercode]

            val wImage = when (wCode) {
                WMO_WeatherCode.CLEAR_SKY,
                WMO_WeatherCode.MAINLY_CLEAR,
                WMO_WeatherCode.PARTLY_CLOUDY ->
                    if (day) wCode?.image + "day" else wCode?.image + "night"

                else -> wCode?.image
            }

            val res = resources
            weatherImage.setImageResource(R.drawable.fog)

            val resID = res.getIdentifier(wImage, "drawable", packageName)
            val drawable = getDrawable(resID)

            weatherImage.setImageDrawable(drawable)
        }
    }
}