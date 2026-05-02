package dam.a51793.coolweatherapp.ui

import android.content.Context
import android.util.Log

data class WeatherCodeInfo(
    val code: Int,
    val description: String,
    val image: String,
)

private val DAY_NIGHT_CODES = setOf(0, 1, 2)

fun loadWeatherCodeMap(context: Context): Map<Int, WeatherCodeInfo> {
    val map = HashMap<Int, WeatherCodeInfo>()
    val allCodes = context.resources.getStringArray(
        context.resources.getIdentifier("weather_codes", "array", context.packageName)
    )
    for (name in allCodes) {
        val resId = context.resources.getIdentifier(name, "array", context.packageName)
        if (resId == 0) {
            Log.e("WeatherApp", "Could not find array resource: $name")
            continue
        }
        val entry = context.resources.getStringArray(resId)
        if (entry.size < 3) {
            Log.e("WeatherApp", "Array $name has fewer than 3 items")
            continue
        }
        val code  = entry[0].trim().toIntOrNull() ?: continue
        val desc  = entry[1].trim()
        val image = entry[2].trim()
        map[code] = WeatherCodeInfo(code, desc, image)
    }
    return map
}

fun resolveWeatherImageName(code: Int, info: WeatherCodeInfo?, isDay: Boolean): String? {
    info ?: return null
    return if (code in DAY_NIGHT_CODES) {
        info.image + if (isDay) "day" else "night"
    } else {
        info.image
    }
}
