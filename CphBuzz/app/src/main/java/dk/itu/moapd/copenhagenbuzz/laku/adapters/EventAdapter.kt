package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseListAdapter
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.interfaces.FavoritedStatusProvider
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import kotlin.random.Random

class EventAdapter(
    private val context: Context,
    private var resource: Int,
    private var data: List<Event>,
    private val onEditEventClicked: (Int) -> Unit,
    private val user: FirebaseUser?,
    private val favoritedStatusProvider: FavoritedStatusProvider
): ArrayAdapter<Event>(
    context,
    R.layout.event_row_item,
    data
){

    private class ViewHolder(view: View){
        val eventLetter: ImageView = view.findViewById(R.id.item_event_letter)
        val title: TextView = view.findViewById(R.id.item_event_title)
        val type: TextView = view.findViewById(R.id.item_event_type)
        val image: ImageView = view.findViewById(R.id.item_image)
        val location: TextView = view.findViewById(R.id.item_event_location)
        val date: TextView = view.findViewById(R.id.item_event_date)
        val description: TextView = view.findViewById(R.id.item_event_description)
        val favoriteBtn: MaterialButton = view.findViewById(R.id.event_btn_favorite)
        val editBtn: MaterialButton = view.findViewById(R.id.button_edit)
        val infoBtn: MaterialButton = view.findViewById(R.id.button_info)
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)

        getItem(position)?.let { event ->
            populateViewHolder(viewHolder, event, position)
        }

        view.tag = viewHolder
        return view
    }

    /**
     * - Loads all relevant event data for each item
     * - Sets visibility
     * - Sets listeners
     */
    private fun populateViewHolder(viewHolder: ViewHolder, event: Event, position: Int) {
        // Fill out the Material Design card.
        loadImages(viewHolder, event)       // Load images using Picasso
        setText(viewHolder, event)          // Set text from Event
        handleFavorites(viewHolder, event)  // Set icon for fave button
        handleEditButton(viewHolder, event) // Set visibility based on login status
        setListeners(viewHolder, position)  // Setup listeners for each button
    }

    private fun setListeners(viewHolder: ViewHolder, position: Int) {
        with(viewHolder){
            editBtn.setOnClickListener{
                onEditEventClicked(position)
            }

            infoBtn.setOnClickListener {
                TODO()
            }

            favoriteBtn.setOnClickListener {
                favoritedStatusProvider.getFavoriteAddedListener().invoke(position)
                notifyDataSetChanged()
            }
        }
    }

    private fun handleEditButton(viewHolder: ViewHolder, event: Event) {
        if(event.userID == user?.uid){
            viewHolder.editBtn.visibility = View.VISIBLE
        } else {
            viewHolder.editBtn.visibility = View.GONE
        }
    }

    private fun handleFavorites(viewHolder: ViewHolder, event: Event) {
        viewHolder.favoriteBtn.setIconResource(
            if (favoritedStatusProvider.isEventFavorited(event)) R.drawable.baseline_favorite_24
            else R.drawable.outline_favorite_border_24
        )

        if(user == null || user.isAnonymous) viewHolder.favoriteBtn.visibility = View.GONE
    }

    private fun setText(viewHolder: ViewHolder, event: Event) {
        with(viewHolder){
            title.text = event.title
            type.text = event.typeString
            description.text = event.description
            location.text = event.location
            date.text = event.dateString
        }
    }

    private fun loadImages(viewHolder: ViewHolder, event: Event) {
        val number = Random.nextInt(1, 501)

        with(viewHolder){
            Picasso.get().load(event.mainImage).into(image)
            Picasso.get().load("https://picsum.photos/seed/$number/150/150").into(eventLetter)
        }
    }

    fun refreshData(favorites: List<Event>){
        data = favorites
        notifyDataSetChanged()
    }
}