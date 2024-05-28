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

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation.Operation.*
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation.Operation.CREATE
import dk.itu.moapd.copenhagenbuzz.laku.models.FavoriteOperation.Operation
import dk.itu.moapd.copenhagenbuzz.laku.models.FavoriteOperation.Operation.*
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

    /**
     * Responsible for database operations
     */
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


    /**
     * Populates the collections from the database on startup.
     */
    private fun initCollections(){
        viewModelScope.launch {
            try {
                _repo.readAllEvents { events ->
                    _events.postValue(events)
                }

                _repo.readAllFavorites { favorites ->
                    _favorites.postValue(favorites)
                }
            } catch (e: Exception) {
                println("Couldn't initialize collections. Error message: $e\"")
            }
        }
    }

    /**
     * Listens for changes to the dataset
     */
    private fun startListeningForEvents() {
        _repo.listenForEvents { changedEvents ->
            val events = _events.value?.toMutableList() ?: mutableListOf()

            changedEvents.events.forEach { event ->
                when(changedEvents.operation){
                    CREATE -> {
                        events.add(event)
                    }

                    UPDATE -> {
                        val index = events.indexOfFirst { it.eventID == event.eventID }
                        events[index] = event
                    }

                    DELETE -> {
                        val index = events.indexOfFirst { it.eventID == event.eventID }
                        events.removeAt(index)
                    }
                }
            }
            _events.postValue(events)
        }
    }

    /**
     * Listens for changes to the dataset
     */
    private fun startListeningForFavorites() {
        _repo.listenForFavorites { changedFavorites ->
            val favorites = _favorites.value?.toMutableList() ?: mutableListOf()

            changedFavorites.events.forEach { event ->
                when(changedFavorites.operation){
                    ADD -> {
                        viewModelScope.launch{
                            try{
                                val newFavorite = _repo.readEvent(event)
                                if(newFavorite != null) favorites.add(newFavorite)
                            } catch (e: Exception){
                                Log.d("DATABASE", "Encountered error when trying to ADD new favorite. Error message: $e\"")
                            }
                        }
                    }

                    REMOVE -> {
                        viewModelScope.launch{
                            try{
                                val deletedFavorite = _repo.readEvent(event)
                                if(deletedFavorite != null) favorites.remove(deletedFavorite)
                            } catch (e: Exception){
                                Log.d("DATABASE", "Encountered error when trying to remove a favorite. Error message: $e\"")
                            }
                        }
                    }
                }
            }

            _favorites.postValue(favorites)
        }
    }

    /**
     * Propagates create event requests from users to the repository
     */
    fun createEvent(event: Event) {
        viewModelScope.launch {
            try{
                _repo.createEvent(event)
            } catch (e: Exception){
                Log.d("DATABASE", "Couldn't create event")
            }
        }
    }

    /**
     * Propagates edit event requests from users to the repository
     */
    fun updateEvent(event: Event){
        viewModelScope.launch {
            try {
                _repo.updateEvent(event)
            } catch (e: Exception) {
                Log.d("DATABASE", "Couldn't update event. Error message: $e\"")
            }
        }
    }

    /**
     * Propagates delete event requests from users to the repository
     */
    fun deleteEvent(event: Event){
        viewModelScope.launch {
            try {
                _repo.deleteEvent(event)
            } catch (e: Exception) {
                Log.d("DATABASE", "Couldn't delete event. Error message: $e\"")
            }
        }
    }

    /**
     * Propagates favorite event requests from users to the repository
     */
    fun addToFavorites(event: Event){
        viewModelScope.launch {
            try {
                _repo.createFavorite(event)
            } catch (e: Exception) {
                Log.d("DATABASE", "Couldn't create favorite event. Error message: $e")
            }
        }
    }

    /**
     * Propagates unfavorite event requests from users to the repository
     */
    fun removeFromFavorites(event: Event){
        viewModelScope.launch {
            try {
                _repo.removeFavorite(event)
            } catch (e: Exception) {
                Log.d("DATABASE", "Couldn't delete favorite event. Error message: $e")
            }
        }
    }


    /**
     * Helper function used to populate UI fields with data from existing events
     */
    fun getEventAtIndex(index: Int): Event {
        val events = _events.value?.toMutableList() ?: mutableListOf()
        return events[index]
    }

    /**
     * Helper function to determine whether or not an event has been favorited by the user.
     * The exposed favorites collection should only contain events stored in the
     * favorites/$uid/ path of the database, and so a .contains call to that collection
     * is used here.
     */
    fun isEventFavorited(event: Event): Boolean{
        val favorites = _favorites.value?.toMutableList() ?: mutableListOf()
        return favorites.contains(event)
    }

    /**
     * Helper method to return current user via auth.
     * - Avoids having all Fragments being dependent on auth as well.
     */
    fun getUser():FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    /**
     * Helper method ensuring clean code when checking login validity.
     */
    fun loggedIn(): Boolean{
        val user = FirebaseAuth.getInstance().currentUser
        return !(user == null || user.isAnonymous)
    }

    /**
     * Cleans up database listeners when finished.
     */
    override fun onCleared() {
        super.onCleared()
        _repo.removeEventListeners()
    }

}