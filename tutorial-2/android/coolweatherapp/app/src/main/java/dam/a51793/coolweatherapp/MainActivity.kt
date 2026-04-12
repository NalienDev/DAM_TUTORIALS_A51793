package dam.a51793.coolweatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    companion object {
        var day = true // this is determined by the current time and sunrise/sunset hours
    }
    private lateinit var viewModel: WeatherViewModel
    private lateinit var latInput: EditText
    private lateinit var longInput: EditText

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.fetchFromGPS()
        else {
            Toast.makeText(this, "Location denied, using default", Toast.LENGTH_SHORT).show()
            viewModel.fetchWeatherData(38.7167f, -9.1333f)
        }
    }
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

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        latInput  = findViewById(R.id.latView)
        longInput = findViewById(R.id.longView)

        viewModel.weatherData.observe(this) { weather ->
            updateUI(weather)
        }

        latInput.setText("38.7167")
        longInput.setText("-9.1333")

        // Observe weather data changes
        viewModel.weatherData.observe(this) { weather ->
            updateUI(weather)
        }

        // Observe errors
        viewModel.error.observe(this) { msg ->
            Log.e("WeatherApp", msg)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        if (savedInstanceState == null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                viewModel.fetchFromGPS()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        findViewById<Button>(R.id.updateBtn).setOnClickListener {
            val lat  = latInput.text.toString().toFloatOrNull()
            val long = longInput.text.toString().toFloatOrNull()
            if (lat == null || long == null) {
                Toast.makeText(this, "Enter valid coordinates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.fetchWeatherData(lat, long)
        }
    }

    private fun loadWeatherCodeMap(): Map<Int, WeatherCodeInfo> {
        val map = HashMap<Int, WeatherCodeInfo>()
        val allCodes = resources.getStringArray(R.array.weather_codes)
        for (name in allCodes) {
            val resId = resources.getIdentifier(name, "array", packageName)
            if (resId == 0) {
                Log.e("WeatherApp", "Could not find array resource: $name")
                continue
            }
            val entry = resources.getStringArray(resId)
            if (entry.size < 3) {
                Log.e("WeatherApp", "Array $name has fewer than 3 items")
                continue
            }
            val code  = entry[0].toIntOrNull() ?: continue
            val desc  = entry[1]
            val image = entry[2]
            map[code] = WeatherCodeInfo(code, desc, image)
        }
        return map
    }

    private fun updateUI(request: WeatherData) {
        val newDay = viewModel.isDay(request)
        if (newDay != day) {
            day = newDay
            recreate()
            return
        }

        val idx = viewModel.getCurrentHourIndex(request)

        findViewById<TextView>(R.id.pressureView).text = request.hourly.pressure_msl[idx].toString() + " hPa"
        findViewById<TextView>(R.id.temperatureView).text = request.hourly.temperature_2m[idx].toString() + "°C"
        findViewById<TextView>(R.id.timeView).text = request.hourly.time[idx]
        findViewById<TextView>(R.id.windDirView).text = request.current_weather.winddirection.toString()
        findViewById<TextView>(R.id.windSpeedView).text = request.current_weather.windspeed.toString()

        latInput.setText(request.latitude)
        longInput.setText(request.longitude)

        val wcMap  = loadWeatherCodeMap()
        val wCode  = wcMap[request.current_weather.weathercode]
        val wImage = when (request.current_weather.weathercode) {
            0, 1, 2 -> if (day) wCode?.image + "day" else wCode?.image + "night"
            else    -> wCode?.image
        }

        val weatherImage = findViewById<ImageView>(R.id.weatherImage)
        val resID = resources.getIdentifier(wImage, "drawable", packageName)
        val drawable = getDrawable(resID)
        weatherImage.setImageDrawable(drawable)
    }
}