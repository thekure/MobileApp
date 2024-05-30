/**
 * MIT License
 *
 * Copyright (c) [2024] [Laurits Kure]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.interfaces.EventBtnListener
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.repositories.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * This class is responsible for populating the individual event objects into the
 * viewholder. It extends FirebaseListAdapter which keeps the data up-to-date.
 *
 * @param context Context
 * @param repository The class responsible for database operations.
 * @param coroutineScope Allows for favoriteBtn listener to interact with db asynchronously
 * @param user Current Firebase user
 * @param onClickListener An interface with listener functions for the different buttons
 */
class EventAdapter(
    options: FirebaseListOptions<Event>,
    private val context: Context?,
    private val repository: EventRepository,
    private val coroutineScope: CoroutineScope,
    private val user: FirebaseUser?,
    private val onClickListener: EventBtnListener

): FirebaseListAdapter<Event>(options){
    private lateinit var faveCallback: (Boolean) -> Unit


    /**
     * Enables favorite status communication with the repository.
     * @param callback Function type for the callback
     */
    fun setFaveCallback(callback: (Boolean) -> Unit) {
        faveCallback = callback
    }

    /**
     * Object that holds all views
     * @param view This is the current view.
     */
    private class ViewHolder(view: View){
        val eventLetter: ImageView = view.findViewById(R.id.item_event_letter)
        val title: TextView = view.findViewById(R.id.item_event_title)
        val type: TextView = view.findViewById(R.id.item_event_type)
        val image: ImageView = view.findViewById(R.id.item_image)
        val location: TextView = view.findViewById(R.id.item_event_location)
        val date: TextView = view.findViewById(R.id.item_event_date)
        val description: TextView = view.findViewById(R.id.item_event_description)
        val favoriteBtn: MaterialButton = view.findViewById(R.id.event_btn_favorite)
        val deleteBtn: MaterialButton = view.findViewById(R.id.button_delete)
        val editBtn: MaterialButton = view.findViewById(R.id.button_edit)
        val infoBtn: MaterialButton = view.findViewById(R.id.button_info)
        val shareBtn: MaterialButton = view.findViewById(R.id.button_share)
    }

    /**
     * - Loads all relevant event data for each item
     * - Sets visibility
     * - Sets listeners
     *
     * @param v Current view
     * @param event The event that holds the data to be populated
     * @param position Current position index in the viewholder
     */
    override fun populateView(v: View, event: Event, position: Int) {
        val viewHolder = ViewHolder(v)

        Log.d("Tag: EventAdapter", "Populating view for event: $event at position: $position")

        loadImages(viewHolder, event)               // Load images using Picasso
        setText(viewHolder, event)                  // Set text from Event
        setVisibility(viewHolder, event)            // Set visibility based on login status
        setListeners(viewHolder, event, position)   // Setup listeners for each button
        checkFavorites(viewHolder, event)
    }

    /**
     * Checks event's favorite status by looking it up in the db.
     * Goes through repository.
     *
     * @param viewHolder Object that holds all views
     * @param event Current event.
     */
    private fun checkFavorites(viewHolder: ViewHolder, event: Event) {
        with(viewHolder){
            repository.isFavorite(event){ isFavorite ->
                Log.d("Tag: EVENT ADAPTER", "Initial isFavorite value: $isFavorite")
                faveCallback(isFavorite)
                if (isFavorite) {
                    Log.d("Tag: EVENT ADAPTER", "Removing event from favorites")
                    favoriteBtn.setIconResource(R.drawable.baseline_favorite_24)
                } else {
                    Log.d("Tag: EVENT ADAPTER", "Creating favorite event")
                    favoriteBtn.setIconResource(R.drawable.outline_favorite_border_24)
                }
            }
        }
    }

    /**
     * Loads main event image from Firebase bucket
     * Loads random image into the "profile picture" called eventLetter
     *
     * @param viewHolder Object that holds all views
     * @param event Current event.
     */
    private fun loadImages(viewHolder: ViewHolder, event: Event) {
        val number = Random.nextInt(1, 501)

        with(viewHolder){
            Picasso.get().load(event.mainImage).into(image)
            Picasso.get().load("https://picsum.photos/seed/$number/150/150").into(eventLetter)
        }
    }

    /**
     * Extracts text fields from event object and populates fields with it
     *
     * @param viewHolder Object that holds all views
     * @param event Current event.
     */
    private fun setText(viewHolder: ViewHolder, event: Event) {
        Log.d("Tag: EVENT ADAPTER", "Set Text")
        with(viewHolder){
            title.text = event.title
            type.text = event.typeString
            description.text = event.description
            location.text = event.location
            date.text = event.dateString
        }
    }

    /**
     * Determines which buttons need to be available to the user from login status.
     *
     * @param viewHolder Object that holds all views
     * @param event Current event.
     */
    private fun setVisibility(viewHolder: ViewHolder, event: Event) {
        Log.d("Tag: EVENT ADAPTER", "Set Visibility")
        with(viewHolder){
            // Edit Button
            if(event.userID == user?.uid){
                editBtn.visibility = View.VISIBLE
            } else {
                editBtn.visibility = View.GONE
            }

            // Delete Button
            if(event.userID == user?.uid){
                viewHolder.deleteBtn.visibility = View.VISIBLE
            } else {
                viewHolder.deleteBtn.visibility = View.GONE
            }
        }
    }

    /**
     * Sets listeners for these buttons:
     * - Edit
     * - Info
     * - Favorite
     * - Delete
     * - Share
     *
     * @param viewHolder Object that holds all views
     * @param event Current event.
     * @param position Current position index in the viewholder
     */
    private fun setListeners(viewHolder: ViewHolder, event: Event,position: Int) {
        Log.d("Tag: EVENT ADAPTER", "Set Listeners")
        with(viewHolder){
            editBtn.setOnClickListener {
                onClickListener.onEditEventClicked(event, position)
            }

            infoBtn.setOnClickListener {
                onClickListener.onInfoEventClicked(event, position)
            }

            favoriteBtn.setOnClickListener {
                Log.d("Tag: EVENT ADAPTER", "Favorite button clicked.")
                coroutineScope.launch(Dispatchers.Main) {
                    repository.isFavorite(event) { isFavorite ->
                        Log.d("Tag: EVENT ADAPTER", "isFavorite value: $isFavorite")
                        faveCallback(isFavorite)
                        if (isFavorite) {
                            Log.d("Tag: EVENT ADAPTER", "Removing event from favorites")
                            repository.removeFavorite(event)
                            favoriteBtn.setIconResource(R.drawable.outline_favorite_border_24)
                        } else {
                            Log.d("Tag: EVENT ADAPTER", "Creating favorite event")
                            repository.createFavorite(event)
                            favoriteBtn.setIconResource(R.drawable.baseline_favorite_24)
                        }
                    }
                }
            }

            deleteBtn.setOnClickListener {
                coroutineScope.launch(Dispatchers.Main) {
                    repository.deleteEvent(event)
                }
            }

            shareBtn.setOnClickListener {
                val intentToSend: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT, "Join me at this great event! It's a ${event.typeString} at '${event.location}'. \n" +
                            "Hope you can make it! It's planned at ${(event.dateString!!)}. \\n\" +" +
                            "Check out the CopenhagenBuzz app for more info, or hit me back!")
                    type = "text/plain"
                }

                val intentToShare = Intent.createChooser(intentToSend, "How do you wish to share?")
                context?.startActivity(intentToShare)
            }
        }
    }
}

