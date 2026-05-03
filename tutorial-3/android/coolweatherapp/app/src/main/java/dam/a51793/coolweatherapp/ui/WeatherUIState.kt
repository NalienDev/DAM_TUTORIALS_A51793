package dam.a51793.coolweatherapp.ui

data class SavedLocation(
    val name: String,
    val latitude: Float,
    val longitude: Float,
)

data class WeatherUIState(
    val latitude: Float = 38.7167f,
    val longitude: Float = -9.1333f,
    val temperature: Float = 0f,
    val windspeed: Float = 0f,
    val winddirection: Int = 0,
    val weathercode: Int = 0,
    val seaLevelPressure: Float = 0f,
    val time: String = "--",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDay: Boolean = true,
    val savedLocations: List<SavedLocation> = emptyList(),
    val activeLocationName: String? = null
    )
