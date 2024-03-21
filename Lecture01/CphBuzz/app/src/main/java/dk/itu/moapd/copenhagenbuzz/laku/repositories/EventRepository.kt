package dk.itu.moapd.copenhagenbuzz.laku.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.laku.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation
import dk.itu.moapd.copenhagenbuzz.laku.models.EventOperation.*
import dk.itu.moapd.copenhagenbuzz.laku.models.FavoriteOperation
import dk.itu.moapd.copenhagenbuzz.laku.models.FavoriteOperation.Operation.*
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
            eventsRef
                .push()
                .key?.let { key ->
                    event.eventID = key
                    eventsRef
                        .child(key)
                        .setValue(event)
                        .addOnSuccessListener {
                            Log.d("DATABASE", "Created event successfully.")
                        }
                }
        }
    }

    fun updateEvent(event: Event) {
        auth.currentUser?.let {
            if(it.uid == event.userID){
                event.eventID?.let { eventID ->
                    eventsRef
                        .child(eventID)
                        .setValue(event)
                        .addOnSuccessListener {
                            Log.d("DATABASE", "Created updated successfully.")
                        }
                }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        eventsRef
            .child(eventId)
            .removeValue()
    }

    suspend fun readAllEvents(callback: (List<Event>) -> Unit) {
        val snapshot = eventsRef.get().await()
        val events = mutableListOf<Event>()
        snapshot.children.forEach { eventSnapshot ->
            val event = eventSnapshot.getValue(Event::class.java)
            event?.let {
                events.add(it)
            }
        }
        callback(events)
    }

    suspend fun readEvent(eventID: String): Event? {
        val snapshot = eventsRef.child(eventID).get().await()
        return snapshot.getValue(Event::class.java)
    }

    suspend fun readAllFavorites(callback: (List<Event>) -> Unit) {
        val user = auth.currentUser
        if(user != null && !user.isAnonymous){
            val snapshot = favoritesRef.child(user.uid).get().await()
            val favoriteIDs = mutableListOf<String>()

            snapshot.children.forEach { favoriteSnapshot ->
                val eventID = favoriteSnapshot.getValue(String::class.java)
                eventID?.let {
                    favoriteIDs.add(it)
                }
            }

            val favorites = mutableListOf<Event>()

            favoriteIDs.forEach { id ->
                val event = readEvent(id)
                if(event != null) favorites.add(event)
            }

            callback(favorites)
        }
    }

    fun createFavorite(event: Event) {
        val user = auth.currentUser

        user?.let {
            favoritesRef
                .push()
                .key?.let { _ ->
                    favoritesRef
                        .child(user.uid)
                        .child(event.eventID!!)
                        .setValue(event.eventID)
                }
        }
    }

    fun removeFavorite(event: Event) {
        val user = auth.currentUser
        user?.let {
            favoritesRef
                .child(user.uid)
                .child(event.eventID!!)
                .removeValue()
                .addOnSuccessListener {
                    Log.d("DATABASE", "Favorite removed successfully.")
                }
                .addOnFailureListener { error ->
                    Log.e("DATABASE", "Error removing favorite", error)
                }
        }
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

    private fun initFavoritesListener(callback: (FavoriteOperation) -> Unit){
        favoritesListener = object : ChildEventListener {
            // Convert DataSnapshot to Event and update DataViewModel with the new event
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = auth.currentUser
                user?.let {
                    val map: HashMap<String, Any>? = snapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                    val keys: List<String>? = map?.keys?.toList()

                    keys?.let {
                        callback(FavoriteOperation(ADD, it))
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val user = auth.currentUser
                user?.let {
                    val map: HashMap<String, Any>? = snapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                    val keys: List<String>? = map?.keys?.toList()
                    keys?.forEach { key ->
                        Log.d("DATABASE", "Key: " + key)

                    }
                    keys?.let {
                        callback(FavoriteOperation(REMOVE, it))
                    }
                }
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

    fun listenForEvents(callback: (EventOperation) -> Unit) {
        initEventListener(callback)
        eventsRef.addChildEventListener(eventsListener)
    }

    fun listenForFavorites(callback: (FavoriteOperation) -> Unit){
        initFavoritesListener(callback)
        favoritesRef.addChildEventListener(favoritesListener)
    }

    fun removeEventListeners() {
        eventsRef.removeEventListener(eventsListener)
        favoritesRef.removeEventListener(favoritesListener)
    }
}