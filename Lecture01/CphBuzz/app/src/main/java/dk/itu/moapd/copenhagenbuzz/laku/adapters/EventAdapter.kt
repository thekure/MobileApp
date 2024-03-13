package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import kotlin.random.Random

class EventAdapter(
    private val context: Context,
    private var resource: Int,
    data: List<Event>,
    private val favoritedListener: (Int) -> Unit
): ArrayAdapter<Event>(
    context,
    R.layout.event_row_item,
    data
) {

    private class ViewHolder(view: View){
        val eventLetter: ImageView = view.findViewById(R.id.item_event_letter)
        val title: TextView = view.findViewById(R.id.item_event_title)
        val type: TextView = view.findViewById(R.id.item_event_type)
        val image: ImageView = view.findViewById(R.id.item_image)
        val location: TextView = view.findViewById(R.id.item_event_location)
        val date: TextView = view.findViewById(R.id.item_event_date)
        val description: TextView = view.findViewById(R.id.item_event_description)
        val favoriteBtn: MaterialButton = view.findViewById(R.id.event_btn_favorite)
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

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event, position: Int) {
        val number = Random.nextInt(1, 501)
        with(viewHolder) {
            // Fill out the Material Design card.
            Picasso.get().load(event.eventImage).into(image)
            title.text = event.eventName
            type.text = event.eventType.toString()
            description.text = event.eventDescription
            Picasso.get().load("https://picsum.photos/seed/$number/150/150").into(eventLetter)
            location.text = event.eventLocation
            date.text = event.eventDate
            favoriteBtn.setOnClickListener {
                favoritedListener.invoke(position)
                notifyDataSetChanged()
            }
            if (event.isFavorited) {
                favoriteBtn.setIconResource(R.drawable.baseline_favorite_24)
            } else favoriteBtn.setIconResource(R.drawable.outline_favorite_border_24)
        }
    }
}