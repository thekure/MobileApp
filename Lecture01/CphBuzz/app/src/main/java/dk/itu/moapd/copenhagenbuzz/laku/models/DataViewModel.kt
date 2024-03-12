/*
 * MIT License
 *
 * Copyright (c) 2024 Laurits Kure
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dk.itu.moapd.copenhagenbuzz.laku.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.javafaker.Faker
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.random.Random


class DataViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * A set of private constants used in this class.
     */
    companion object {
        private const val ALL_EVENTS = "ALL_EVENTS"
        private const val FAVORITE_EVENTS = "FAVORITE_EVENTS"
    }

    /**
     * Allows for updates to the data.
     */
    private val _events: MutableLiveData<List<Event>> by lazy {
        savedStateHandle.getLiveData(ALL_EVENTS, ArrayList())
    }

    private val _favorites: MutableLiveData<List<Event>> by lazy {
        savedStateHandle.getLiveData(FAVORITE_EVENTS, ArrayList())
    }


    /**
     * A LiveData object to hold a list of events.
     */
    val events: LiveData<List<Event>> = _events
    val favorites: LiveData<List<Event>> = _favorites

    init{
        fetchEvents()
        // REMOVE THIS CODE WHEN FAVORITES WORK
        val faker = Faker()
        val eventList = mutableListOf<Event>()
        repeat(2) {
            val number = Random.nextInt(1, 501)
            val event = Event(
                eventName = faker.lorem().word(),
                eventLocation = faker.address().city(),
                eventDate = faker.date().toString(),
                eventType = EventType.BIRTHDAY,
                eventDescription = faker.lorem().word(),
                isFavorited = true,
                eventImage = "https://picsum.photos/seed/$number/400/194"
            )
            eventList.add(event)
        }
        _favorites.value = eventList
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            try {
                _events.value = generateDummyEvents()
                _favorites.value = getFavorites()
            } catch (e: Exception){
                println("Couldn't fetch events: $e")
            }
        }

    }

    private fun generateDummyEvents(): List<Event> {
        // Generate dummy events here
        val faker = Faker()
        val eventList = mutableListOf<Event>()
        repeat(10) {
            val number = Random.nextInt(1, 501)
            val event = Event(
                eventName = faker.lorem().word(),
                eventLocation = faker.address().city(),
                eventDate = faker.date().toString(),
                eventType = EventType.WEDDING,
                eventDescription = faker.lorem().word(),
                isFavorited = false,
                eventImage = "https://picsum.photos/seed/$number/400/194"
            )
            eventList.add(event)
        }
        return eventList
    }

    /**
     * Return a list of favorited events.
     */
    private fun getFavorites(): List<Event> {
        return _events.value?.filter { it.isFavorited } ?: emptyList()
    }
}