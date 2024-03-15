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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.laku.DATABASE_URL
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.Calendar
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

    // Initialize Firebase Auth and connect to the Firebase Realtime Database.
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.database(DATABASE_URL).reference

    init{
        fetchEvents()
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
        repeat(2) {
            val number = Random.nextInt(1, 501)
            val dates = getFakeDates()

            val event = Event(
                title = faker.lorem().word(),
                location = faker.address().city(),
                startDate = dates.first,
                endDate = dates.second,
                type = Random.nextInt(0, 2),
                description = faker.lorem().word(),
                isFavorited = false,
                mainImage = "https://picsum.photos/seed/$number/400/194",
                "Faker"
            )
            eventList.add(event)
        }
        repeat(2) {
            val number = Random.nextInt(1, 501)
            val dates = getFakeDates()
            val event = Event(
                title = faker.lorem().word(),
                location = faker.address().city(),
                startDate = dates.first,
                endDate = dates.second,
                type = Random.nextInt(0, 2),
                description = faker.lorem().word(),
                isFavorited = true,
                mainImage = "https://picsum.photos/seed/$number/400/194",
                "Faker"
            )
            eventList.add(event)
        }
        return eventList
    }

    private fun getFakeDates(): Pair<Long, Long>{
        // Get the current date
        var cal = Calendar.getInstance()
        val seed = Random.nextInt(1, 31)
        cal.add(Calendar.DAY_OF_YEAR, seed)
        val startDateDate = cal.time

        // Add a random number of days (up to 7) to the first date to get the second date
        cal = Calendar.getInstance()
        cal.time = startDateDate
        cal.add(Calendar.DAY_OF_YEAR, Random.nextInt(1, 8))
        val endDateDate = cal.time

        // Convert dates to Long values (milliseconds since the Unix epoch)
        val startDate = startDateDate.time
        val endDate = endDateDate.time

        return Pair(startDate, endDate)
    }

    private fun getFavorites(): List<Event> {
        return _events.value?.filter { it.isFavorited } ?: emptyList()
    }

    fun invertIsFavorited(event: Event) {
        event.isFavorited = !event.isFavorited
        _favorites.value = getFavorites()
    }

    fun createEvent(event: Event) {
        val events = _events.value?.toMutableList() ?: mutableListOf()
        events.add(event)
        _events.postValue(events)

        // If the user is authenticated, create a new unique key for the object in the database.
        auth.currentUser?.let { user ->
            db.child("events")
                .child(user.uid)
                .push()
                .key?.let { uid ->
                    // Insert the object in the database.
                    db.child("events")
                        .child(user.uid)
                        .child(uid)
                        .setValue(event)
                }
        }
    }

    fun updateEvent(position: Int, updatedEvent: Event){
        val events = _events.value?.toMutableList() ?: mutableListOf()
        events[position] = updatedEvent
        _events.postValue(events)

    }

    fun getEvent(position: Int): Event {
        val events = _events.value?.toMutableList() ?: mutableListOf()
        return events[position]
    }

    fun getUser():FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun loggedIn(): Boolean{
        val user = FirebaseAuth.getInstance().currentUser
        return !(user == null || user.isAnonymous)
    }

    fun getEmptyEvent(): Event{
        return Event(
            title = "",
            location = "",
            startDate = 0,
            endDate = 0,
            type = Random.nextInt(0, 2),
            description = "",
            isFavorited = false,
            mainImage = "",
            userID = "Created By getEmptyEvent"
        )
    }

  /* 
     {
      "Title": "Test",
      "Location": "Fart City",
      "StartDate": 1677897600000,
      "EndDate": 1677984000000,
      "Type": 1,
      "Description": "Some random text",
      "ImageURL": "https://picsum.photos/seed/173/400/194",
      "UserID": "bmw78KmygbbnUdvi1U69rWjylJy1"

     }
  */
}