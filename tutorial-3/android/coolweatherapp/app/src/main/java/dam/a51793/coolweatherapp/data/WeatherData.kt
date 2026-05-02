package dam.a51793.coolweatherapp.data

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val latitude: String,
    val longitude: String,
    val timezone: String,
    val current_weather: CurrentWeather,
    val hourly: Hourly,
    val daily: Daily,
)

@Serializable
data class Daily(
    val sunrise: List<String>,
    val sunset: List<String>,
)

@Serializable
data class CurrentWeather(
    val temperature: Float,
    val windspeed: Float,
    val winddirection: Int,
    val weathercode: Int,
    val time: String,
)

@Serializable
data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Float>,
    val weathercode: List<Int>,
    val pressure_msl: List<Double>,
)

data class WeatherCodeInfo(
    val code: Int,
    val description: String,
    val image: String,
)