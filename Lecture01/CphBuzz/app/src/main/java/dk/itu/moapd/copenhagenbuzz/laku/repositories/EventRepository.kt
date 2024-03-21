package dk.itu.moapd.copenhagenbuzz.laku.repositories

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.laku.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EventRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.database(DATABASE_URL).reference.child("copenhagen_buzz")
    private val eventsRef = db.child("events")
    private lateinit var eventsListener : ChildEventListener
    private val favoritesRef = db.child("favorites")
    private lateinit var favoritesListener : ChildEventListener

    fun createEvent(event: Event) {
        auth.currentUser?.let {
            db.child("events")
                .push()
                .key?.let { key ->
                    event.eventID = key
                    db.child("events")
                        .child(key)
                        .setValue(event)
                        .addOnSuccessListener {
                            Log.d("DATABASE", "Created event successfully.")
                        }
                }
        }
    }

    fun updateEvent(event: Event) {
        event.eventID?.let { eventId ->
            db.child("events").child(eventId).setValue(event)
        }
    }

    fun deleteEvent(eventId: String) {
        db.child("events").child(eventId).removeValue()
    }

    suspend fun readAllEvents(): DataSnapshot {
        return eventsRef.get().await()
    }

    suspend fun readEvent(eventId: String, listener: ValueEventListener) {
        db.child("events").child(eventId).addListenerForSingleValueEvent(listener)
    }

    // Favorites CRUD operations

    suspend fun addFavorite(userId: String, favoriteID: String) {
        db.child("favorites").child(userId).push().setValue(favoriteID)
    }

    fun removeFavorite(userId: String, favoriteId: String) {
        db.child("favorites").child(userId).child(favoriteId).removeValue()
    }

    fun getFavorites(userId: String, listener: ValueEventListener) {
        db.child("favorites").child(userId).addListenerForSingleValueEvent(listener)
    }

    private fun initEventListener(callback: (EventOperation) -> Unit){
        eventsListener = object : ChildEventListener {
            // Convert DataSnapshot to Event and update DataViewModel with the new event
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val event = snapshot.getValue(Event::class.java)
                event?.let {
                    callback(EventOperation(Operation.CREATE, listOf(it)))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                val updatedEvent = snapshot.getValue(Event::class.java)
                updatedEvent?.let {
                    callback(EventOperation(Operation.UPDATE, listOf(it)))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedEvent = snapshot.getValue(Event::class.java)
                removedEvent?.let {
                    callback(EventOperation(Operation.DELETE, listOf(it)))
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Irrelevant
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        }
    }

    private fun initFavoritesListener(callback: (EventOperation) -> Unit){
        favoritesListener = object : ChildEventListener {
            // Convert DataSnapshot to Event and update DataViewModel with the new event
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val event = snapshot.getValue(Event::class.java)
                event?.let {
                    callback(EventOperation(Operation.CREATE, listOf(it)))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle event removal
                // You may want to remove the event from the ViewModel
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Irrelevant
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Irrelevant
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        }
    }

    fun initEventCollection(callback: (List<Event>) -> Unit) {
        // Fetch initial snapshot to load existing events
        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                snapshot.children.forEach { eventSnapshot ->
                    val event = eventSnapshot.getValue(Event::class.java)
                    event?.let {
                        events.add(it)
                    }
                }
                // Callback to ViewModel with initial list of events
                callback(events)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun initFavoriteCollection(callback: (List<Event>) -> Unit) {
        // Fetch initial snapshot to load existing favorites
        favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = mutableListOf<Event>()
                snapshot.children.forEach { favoriteSnapshot ->
                    val event = favoriteSnapshot.getValue(Event::class.java)
                    event?.let {
                        favorites.add(it)
                    }
                }
                // Callback to ViewModel with initial list of favorites
                callback(favorites)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun listenForEvents(callback: (EventOperation) -> Unit) {
        initEventListener(callback)
        eventsRef.addChildEventListener(eventsListener)
    }

    fun listenForFavorites(callback: (EventOperation) -> Unit){
        initFavoritesListener(callback)
        favoritesRef.addChildEventListener(favoritesListener)
    }

    fun removeEventListeners() {
        eventsRef.removeEventListener(eventsListener)
        favoritesRef.removeEventListener(favoritesListener)
    }
}
