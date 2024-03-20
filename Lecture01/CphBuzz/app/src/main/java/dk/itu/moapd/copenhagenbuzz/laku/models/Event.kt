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

/**
 * Contains the relevant fields of an event object.
 */
data class Event(
    var eventID: String? = null,
    var userID: String? = null,
    var title: String? = null,
    var location: String? = null,
    var startDate: Long? = null,
    var endDate: Long? = null,
    var dateString: String? = null,
    var typeString: String? = null,
    var type: Int? = null,
    var description: String? = null,
    var mainImage: String? = null,
    var isFavorited: Boolean? = null
)