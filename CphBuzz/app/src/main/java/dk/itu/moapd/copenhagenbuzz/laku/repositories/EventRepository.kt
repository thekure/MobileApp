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

/**
 * Handles database access, CRUD operations.
 */
class EventRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = Firebase.database(DATABASE_URL).reference.child("copenhagen_buzz")
    private val eventsRef    = db.child("events")
    private val favoritesRef = db.child("favorites")
    private lateinit var eventsListener     : ChildEventListener
    private lateinit var favoritesListener  : ChildEventListener

    /**
     * CRUD: Create one event.
     */
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

    /**
     * CRUD: Update one event.
     */
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

    /**
     * CRUD: Delete one event.
     */
    fun deleteEvent(event: Event) {
        val user = auth.currentUser
        if(user != null && event.userID == user.uid){
            user.let {
                eventsRef
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
    }

    /**
     * CRUD: Read all events.
     */
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

    /**
     * CRUD: Read one event.
     */
    suspend fun readEvent(eventID: String): Event? {
        val snapshot = eventsRef.child(eventID).get().await()
        return snapshot.getValue(Event::class.java)
    }

    /**
     * CRUD: Read all favorites stored by user.
     */
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

    /**
     * CRUD: Create one favorite.
     */
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

    /**
     * CRUD: Delete one favorite.
     */
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

    /**
     * Sets up a ChildEventListener that reports any changes in the database/events path.
     */
    private fun initEventListener(callback: (EventOperation) -> Unit){
        eventsListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("DATABASE", "Event listener triggered onChildAdded.")
                val event = snapshot.getValue(Event::class.java)
                event?.let {
                    callback(EventOperation(Operation.CREATE, listOf(it)))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("DATABASE", "Event listener triggered onChildChanged.")
                val updatedEvent = snapshot.getValue(Event::class.java)
                updatedEvent?.let {
                    callback(EventOperation(Operation.UPDATE, listOf(it)))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("DATABASE", "Event listener triggered onChildRemoved.")
                val removedEvent = snapshot.getValue(Event::class.java)
                removedEvent?.let {
                    callback(EventOperation(Operation.DELETE, listOf(it)))
                }
            }

            /**
             * Unused.
             */
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Irrelevant
            }

            override fun onCancelled(e: DatabaseError) {
                Log.d("DATABASE", "Event listener encountered an error: $e")
            }
        }
    }

    /**
     * Sets up a ChildEventListener that reports any changes in the database/favorites path.
     */
    private fun initFavoritesListener(callback: (FavoriteOperation) -> Unit){
        favoritesListener = object : ChildEventListener {

            /**
             * Favorites are stored as strings in the favorites table. Because each entry needs to
             * have a key though, these strings are stored in both the key and value position,
             * making the table hold a HashMap<String, String>.
             *
             * When new favorites are added, this function gets the eventIDs by collecting the
             * key set, and sending them back in a List<String>.
             */
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("DATABASE", "Favorites listener triggered onChildAdded.")
                val user = auth.currentUser
                user?.let {
                    val eventID = snapshot.getValue(String::class.java)
                    eventID?.let {
                        callback(FavoriteOperation(ADD, listOf(it)))
                    }
                }
            }

            /**
             * Favorites are stored as strings in the favorites table. When a favorite is removed,
             * this function gets the eventID from the snapshot sends it back in a List<String>.
             */
            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("DATABASE", "Favorites listener triggered onChildRemoved.")
                val user = auth.currentUser
                user?.let {
                    val eventID = snapshot.getValue(String::class.java)

                    eventID?.let {
                        callback(FavoriteOperation(REMOVE, listOf(it)))
                    }
                }
            }

            /**
             * Unused.
             */
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("DATABASE", "Favorites listener triggered onChildChanged.")
                // Irrelevant
            }

            /**
             * Unused.
             */
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("DATABASE", "Favorites listener triggered onChildMoved.")
                // Irrelevant
            }

            override fun onCancelled(e: DatabaseError) {
                Log.d("DATABASE", "Favorites listener encountered an error: $e")
            }
        }
    }

    /**
     * Exposed method for initializing database listeners.
     */
    fun listenForEvents(callback: (EventOperation) -> Unit) {
        initEventListener(callback)
        eventsRef.addChildEventListener(eventsListener)
    }

    /**
     * Exposed method for initializing database listeners.
     */
    fun listenForFavorites(callback: (FavoriteOperation) -> Unit){
        initFavoritesListener(callback)
        // Listen on favorites/$uid path
        if(auth.currentUser?.uid != null){
            favoritesRef.child(auth.currentUser!!.uid).addChildEventListener(favoritesListener)
        }
    }

    /**
     * DataViewModel calls this to clean up listeners onCleared.
     */
    fun removeEventListeners() {
        eventsRef.removeEventListener(eventsListener)
        favoritesRef.removeEventListener(favoritesListener)
    }
}