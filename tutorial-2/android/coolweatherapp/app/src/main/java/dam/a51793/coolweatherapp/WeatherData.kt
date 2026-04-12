package dam.a51793.coolweatherapp

data class WeatherData(
    var latitude: String,
    var longitude: String,
    var timezone: String,
    var current_weather: CurrentWeather,
    var hourly: Hourly,
    var daily: Daily
)

data class Daily(
    var sunrise: ArrayList<String>,
    var sunset: ArrayList<String>
)
data class CurrentWeather(
    var temperature: Float,
    var windspeed: Float,
    var winddirection: Int,
    var weathercode: Int,
    var time: String
)

data class Hourly(
    var time: ArrayList<String>,
    var temperature_2m: ArrayList<Float>,
    var weathercode: ArrayList<Int>,
    var pressure_msl: ArrayList<Double>
)

data class WeatherCodeInfo(
    val code: Int,
    val description: String,
    val image: String
)