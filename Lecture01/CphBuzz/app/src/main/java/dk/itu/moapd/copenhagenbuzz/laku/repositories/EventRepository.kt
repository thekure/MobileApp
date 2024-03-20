package dk.itu.moapd.copenhagenbuzz.laku.repositories

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EventRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val eventsRef = db.child("copenhagen_buzz").child("events")
    private lateinit var eventsListener : ChildEventListener
    private val favoritesRef = db.child("copenhagen_buzz").child("favorites")
    private lateinit var favoritesListener : ChildEventListener

    // Events CRUD operations

    suspend fun createEvent(event: Event) {
        auth.currentUser?.let { _ ->
            db.child("copenhagen_buzz")
                .child("events")
                .push()
                .key?.let { key ->
                    event.eventID = key
                    db.child("copenhagen_buzz")
                        .child("events")
                        .child(key)
                        .setValue(event)
                        .addOnCompleteListener {
                            Log.d("Event", (event.title ?: "null"))
                            Log.d("Event", (event.eventID ?: "null"))
                            Log.d("Event", (event.description ?: "null"))
                            Log.d("Event", (event.location ?: "null"))
                            Log.d("Event", (event.mainImage ?: "null"))
                            Log.d("Event", (event.userID ?: "null"))
                            Log.d("Event", (event.type.toString()))
                            Log.d("Event", (event.typeString ?: "null"))
                            Log.d("Event", (event.endDate.toString() ?: "null"))
                            Log.d("Event", (event.startDate.toString() ?: "null"))
                            Log.d("Event", (event.dateString ?: "null"))
                        }
                        .addOnFailureListener {
                            Log.d("Event", (event.title ?: "null"))
                            Log.d("Event", (event.eventID ?: "null"))
                            Log.d("Event", (event.description ?: "null"))
                            Log.d("Event", (event.location ?: "null"))
                            Log.d("Event", (event.mainImage ?: "null"))
                            Log.d("Event", (event.userID ?: "null"))
                            Log.d("Event", (event.type.toString()))
                            Log.d("Event", (event.typeString ?: "null"))
                            Log.d("Event", (event.endDate.toString() ?: "null"))
                            Log.d("Event", (event.startDate.toString() ?: "null"))
                            Log.d("Event", (event.dateString ?: "null"))
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

    fun getAllFavorites(userId: String, listener: ValueEventListener) {
        db.child("favorites").child(userId).addListenerForSingleValueEvent(listener)
    }

    private fun initEventListener(callback: (List<Event>) -> Unit){
        eventsListener = object : ChildEventListener {
            // Convert DataSnapshot to Event and update DataViewModel with the new event
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val event = snapshot.getValue(Event::class.java)
                event?.let {
                    callback(listOf(it))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle event update
                // You may want to update the existing event in the ViewModel
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle event removal
                // You may want to remove the event from the ViewModel
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Irrelevant
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        }
    }

    private fun initFavoritesListener(callback: (List<Event>) -> Unit){
        favoritesListener = object : ChildEventListener {
            // Convert DataSnapshot to Event and update DataViewModel with the new event
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val event = snapshot.getValue(Event::class.java)
                event?.let {
                    callback(listOf(it))
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

    fun listenForEvents(callback: (List<Event>) -> Unit) {
        initEventListener(callback)
        eventsRef.addChildEventListener(eventsListener)
    }

    fun listenForFavorites(callback: (List<Event>) -> Unit){
        initFavoritesListener(callback)
        favoritesRef.addChildEventListener(favoritesListener)
    }

    fun removeEventListeners() {
        eventsRef.removeEventListener(eventsListener)
        favoritesRef.removeEventListener(favoritesListener)
    }
}
