/**
 * MIT License
 *
 * Copyright (c) [2024] [Laurits Kure]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dk.itu.moapd.copenhagenbuzz.laku.utilities

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Utility class for calling OpenMeteoAPI for weather data.
 */
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