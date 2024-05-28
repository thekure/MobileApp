package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.content.Context
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

class EventAdapter(
    options: FirebaseListOptions<Event>,
    private val context: Context?,
    private val repository: EventRepository,
    private val coroutineScope: CoroutineScope,
    private val user: FirebaseUser?,
    private val onClickListener: EventBtnListener

): FirebaseListAdapter<Event>(options){
    private lateinit var favoriteCallback: (Boolean) -> Unit

    fun setFavoriteCallback(callback: (Boolean) -> Unit) {
        favoriteCallback = callback
    }

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
    }

    /**
     * - Loads all relevant event data for each item
     * - Sets visibility
     * - Sets listeners
     */
    override fun populateView(v: View, event: Event, position: Int) {
        val viewHolder = ViewHolder(v)

        loadImages(viewHolder, event)               // Load images using Picasso
        setText(viewHolder, event)                  // Set text from Event
        setVisibility(viewHolder, event)            // Set visibility based on login status
        setListeners(viewHolder, event, position)   // Setup listeners for each button
    }

    private fun loadImages(viewHolder: ViewHolder, event: Event) {
        val number = Random.nextInt(1, 501)

        with(viewHolder){
            Picasso.get().load(event.mainImage).into(image)
            Picasso.get().load("https://picsum.photos/seed/$number/150/150").into(eventLetter)
        }
    }

    private fun setText(viewHolder: ViewHolder, event: Event) {
        Log.d("EVENT ADAPTER", "Set Text")
        with(viewHolder){
            title.text = event.title
            type.text = event.typeString
            description.text = event.description
            location.text = event.location
            date.text = event.dateString
        }
    }

    private fun setVisibility(viewHolder: ViewHolder, event: Event) {
        Log.d("EVENT ADAPTER", "Set Visibility")
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

    private fun setListeners(viewHolder: ViewHolder, event: Event,position: Int) {
        Log.d("EVENT ADAPTER", "Set Listeners")
        with(viewHolder){
            Log.d("EVENT ADAPTER", "Set editBtn Listener")
            editBtn.setOnClickListener {
                onClickListener.onEditEventClicked(event, position)
            }

            Log.d("EVENT ADAPTER", "Set infoBtn Listener")
            infoBtn.setOnClickListener {
                onClickListener.onInfoEventClicked(event, position)
            }

            Log.d("EVENT ADAPTER", "Set favoriteBtn Listener")
            favoriteBtn.setOnClickListener {
                coroutineScope.launch(Dispatchers.Main) {
                    repository.isFavorite(event) { isFavorite ->
                        favoriteCallback(isFavorite)
                        if (isFavorite) {
                            repository.removeFavorite(event)
                            favoriteBtn.setIconResource(R.drawable.outline_favorite_border_24)
                        } else {
                            repository.createFavorite(event)
                            favoriteBtn.setIconResource(R.drawable.baseline_favorite_24)
                        }
                    }
                }
            }

            Log.d("EVENT ADAPTER", "Set deleteBtn Listener")
            favoriteBtn.setOnClickListener {
                coroutineScope.launch(Dispatchers.Main) {
                    repository.deleteEvent(event)
                }
            }
        }
    }
}

