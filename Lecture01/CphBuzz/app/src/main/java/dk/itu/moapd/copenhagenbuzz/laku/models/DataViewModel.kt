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

import android.util.Log
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
import dk.itu.moapd.copenhagenbuzz.laku.repositories.EventRepository
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
    private val repository = EventRepository()

    init{
        initCollections()
        startListeningForEvents()
        startListeningForFavorites()
    }

    private fun initCollections(){
        viewModelScope.launch {
            try {
                repository.initEventCollection { events ->
                    _events.postValue(events)
                }

                repository.initFavoriteCollection { favorites ->
                    _favorites.postValue(favorites)
                }
            } catch (e: Exception) {
                println("Couldn't initialize collections: $e")
            }
        }
    }

    private fun startListeningForEvents() {
        repository.listenForEvents { events ->
            _events.postValue(events)
        }
    }

    private fun startListeningForFavorites() {
        repository.listenForFavorites { favorites ->
            _favorites.postValue(favorites)
        }
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            try {
                repository.createEvent(event)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateEvent(event: Event){
        viewModelScope.launch {
            try {
                repository.updateEvent(event)
            } catch (e: Exception) {
                // Handle error
            }
        }

    }

    fun getEvent(position: Int): Event {
        val events = _events.value?.toMutableList() ?: mutableListOf()
        return events[position]
    }

    fun removeFromFavorites(event: Event){
        TODO()
    }

    fun getUser():FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun loggedIn(): Boolean{
        val user = FirebaseAuth.getInstance().currentUser
        return !(user == null || user.isAnonymous)
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeEventListeners()
    }

}