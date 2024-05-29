package dk.itu.moapd.copenhagenbuzz.laku.utilities

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class WeatherUtil {

    data class OpenMeteoResponse(
        @SerializedName("current") val current: CurrentWeather
    )

    data class CurrentWeather(
        @SerializedName("temperature_2m") val temperature: Double
    )

    object OpenMeteoApi {
        private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
        private val client = OkHttpClient()

        fun getCurrentTemperature(lat: Double, lon: Double, callback: (Double?) -> Unit) {
            val url = "$BASE_URL?latitude=$lat&longitude=$lon&current=temperature_2m"
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    callback(null)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.body?.let {
                        val weatherResponse = Gson().fromJson(it.charStream(), OpenMeteoResponse::class.java)
                        callback(weatherResponse.current.temperature)
                    } ?: callback(null)
                }
            })
        }
    }
}