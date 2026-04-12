package dam.a51793.coolweatherapp

import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    val day = true //TODO this is determined by the current time and sunrise/sunset hours
    override fun onCreate(savedInstanceState: Bundle?) {
        val lat = 38.7167f
        val long = -9.1333f

        val weatherData = fetchWeatherData(lat, long)

        //TODO change day val here

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

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=$lat&longitude=$long&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
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
            val weather = WeatherAPI_Call(lat, long)
            updateUI(weather)
        }
    }

    private fun updateUI(request: WeatherData) {
        runOnUiThread {
            val weatherImage: ImageView = findViewById(R.id.weatherImage)
            val pressure: TextView = findViewById(R.id.pressureValue)

            // TODO ...
            pressure.text = request.hourly.pressure_msl[12].toString() + " hPa"
            // TODO ...

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
            // TODO ...
        }
    }
}