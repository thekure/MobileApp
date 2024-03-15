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

package dk.itu.moapd.copenhagenbuzz.laku.models

import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This class denotes the different event types.
 * Matches the array of strings in strings.xml
 */
enum class EventType {
    BIRTHDAY,
    WEDDING,
    CONFERENCE
}

/**
 * Contains the relevant fields of an event object.
 */
data class Event(
    var title: String,
    var location: String,
    var startDate: Long,
    var endDate: Long,
    //var date: String,
    var type: EventType,
    var description: String,
    var isFavorited: Boolean,
    var mainImage: String,
    var userID: FirebaseUser?
) {

    /**
     * Custom toString function.
     */
    override fun toString(): String {
        return "Event (eventName = ’$title’, " +
                "location = ’$location’) " +
                "date = ’${getDateString()}’) " +
                "type = ’$type’) " +
                "description = ’$description’)"
    }

    /**
     * Returns the index of the event type
     */
    fun getTypeIndex(): Int{
        return when (type){
            EventType.BIRTHDAY -> 0
            EventType.WEDDING -> 1
            EventType.CONFERENCE -> 2
        }
    }

    /**
     * Helper function to convert dates from long to string.
     */
    fun getDateString(): String{
        /**
         * Defines the wanted display format for the dates.
         * Currently set to: EEE, MMM dd yyyy.
         */
        val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH)
        val startDateAsString = dateFormat.format(Date(startDate))
        val endDateAsString = dateFormat.format(Date(endDate))

        if(startDateAsString == endDateAsString) return startDateAsString

        return "$startDateAsString - $endDateAsString"
    }
}