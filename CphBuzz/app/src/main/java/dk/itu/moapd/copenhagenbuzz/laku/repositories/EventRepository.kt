package dk.itu.moapd.copenhagenbuzz.laku.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.laku.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.laku.models.Event

/**
 * Handles database access, CRUD operations.
 */
class EventRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.database(DATABASE_URL).reference.child("copenhagen_buzz")
    private val eventsRef = db.child("events")
    private val favoritesRef = db.child("favorites")

    /**
     * CRUD: Create one event.
     */
    fun createEvent(event: Event) {
        auth.currentUser?.let {
            eventsRef
                .push()
                .key?.let { key ->
                    Log.d("Tag: DATABASE", "Generating eventID: $key")
                    event.eventID = key
                    eventsRef
                        .child(key)
                        .setValue(event)
                        .addOnSuccessListener {
                            Log.d("Tag: DATABASE", "Created event successfully.")
                        }
                        .addOnFailureListener {
                            Log.d("Tag: DATABASE", "Event creation failed.")
                        }
                        .addOnCanceledListener {
                            Log.d("Tag: DATABASE", "Event creation cancelled.")
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Tag: DATABASE", "Event creation completed successfully.")
                            } else {
                                Log.e(
                                    "Tag: DATABASE",
                                    "Tag: Event creation completed with errors. Exception: ${task.exception?.message}"
                                )
                            }
                        }
                } ?: Log.e("Tag: DATABASE", "Failed to generate a key for the event.")
        } ?: Log.e("Tag: DATABASE", "User is not authenticated.")
    }

    /**
     * CRUD: Update one event.
     */
    fun updateEvent(event: Event) {
        auth.currentUser?.let {
            if (it.uid == event.userID) {
                event.eventID?.let { eventID ->
                    eventsRef
                        .child(eventID)
                        .setValue(event)
                        .addOnSuccessListener {
                            Log.d("Tag: DATABASE", "Created updated successfully.")
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
        if (user != null && event.userID == user.uid) {
            user.let {
                eventsRef
                    .child(event.eventID!!)
                    .removeValue()
                    .addOnSuccessListener {
                        Log.d("Tag: DATABASE", "Event removed successfully.")
                    }
                    .addOnFailureListener { error ->
                        Log.e("Tag: DATABASE", "Error removing favorite", error)
                    }
            }
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
                .key?.let { key ->
                    Log.d("Tag: DATABASE", "Generating eventID: $key")
                    favoritesRef
                        .child(user.uid)
                        .child(event.eventID!!)
                        .setValue(event)
                }
        }
    }

    /**
     * CRUD: Delete one favorite.
     */
    fun removeFavorite(event: Event) {
        val user = auth.currentUser
        user?.let {
            Log.d("Tag: DATABASE", "Event ID: " + event.eventID)
            favoritesRef
                .child(user.uid)
                .child(event.eventID!!)
                .removeValue()
                .addOnSuccessListener {
                    Log.d("Tag: DATABASE", "Favorite removed successfully.")
                }
                .addOnFailureListener { error ->
                    Log.e("Tag: DATABASE", "Error removing favorite", error)
                }
        }
    }

    fun isFavorite(event: Event, callback: (Boolean) -> Unit) {
        val user = auth.currentUser

        user?.uid?.let { uid ->
            favoritesRef
                .child(uid)
                .get()
                .addOnSuccessListener { result ->
                    val isFavorite = result.children.any { it.key == event.userID }
                    callback(isFavorite)
                }
                .addOnCanceledListener {
                    Log.d("Tag: DATABASE", "Error while checking favorited status.")
                }
        }
    }
}

