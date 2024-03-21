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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation.*
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation.Operation.*
import dk.itu.moapd.copenhagenbuzz.laku.repositories.EventRepository
import kotlinx.coroutines.launch
import kotlin.Exception


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

    private val _repo = EventRepository()


    /**
     * A LiveData object to hold a list of events.
     */
    val events: LiveData<List<Event>> = _events
    val favorites: LiveData<List<Event>> = _favorites

    init{
        initCollections()
        startListeningForEvents()
        startListeningForFavorites()
    }

    private fun initCollections(){
        viewModelScope.launch {
            try {
                _repo.initEventCollection { events ->
                    _events.postValue(events)
                }

                _repo.initFavoriteCollection { favorites ->
                    _favorites.postValue(favorites)
                }
            } catch (e: Exception) {
                println("Couldn't initialize collections: $e")
            }
        }
    }

    private fun startListeningForEvents() {
        _repo.listenForEvents { newEvents ->
            val events = _events.value?.toMutableList() ?: mutableListOf()

            newEvents.events.forEach { newEvent ->
                when(newEvents.operation){
                    CREATE -> {
                        events.add(newEvent)
                    }

                    UPDATE -> {
                        val index = events.indexOfFirst { it.eventID == newEvent.eventID }
                        events[index] = newEvent
                    }

                    DELETE -> {
                        val index = events.indexOfFirst { it.eventID == newEvent.eventID }
                        events.removeAt(index)
                    }
                }
            }
            _events.postValue(events)
        }
    }

    private fun startListeningForFavorites() {
        _repo.listenForFavorites { newFavorites ->
            val favorites = _events.value?.toMutableList() ?: mutableListOf()

            newFavorites.events.forEach { newEvent ->
                when(newFavorites.operation){
                    CREATE -> {
                        favorites.add(newEvent)
                    }

                    UPDATE -> {
                        val index = favorites.indexOfFirst { it.eventID == newEvent.eventID }
                        favorites[index] = newEvent
                    }

                    DELETE -> {
                        val index = favorites.indexOfFirst { it.eventID == newEvent.eventID }
                        favorites.removeAt(index)
                    }
                }
            }
            _events.postValue(favorites)
        }
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            try{
                _repo.createEvent(event)
            } catch (e: Exception){
                Log.d("DATABASE", "Couldn't create event")
            }
        }
    }

    fun updateEvent(event: Event){
        viewModelScope.launch {
            try {
                _repo.updateEvent(event)
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
        _repo.removeEventListeners()
    }

}