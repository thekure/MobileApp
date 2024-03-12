package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import kotlin.random.Random

class EventAdapter(
    private val context: Context,
    private var resource: Int,
    private val model: DataViewModel
): BaseAdapter() {

    private var events: List<Event> = emptyList()

    private class ViewHolder(view: View){
        val eventLetter: ImageView = view.findViewById(R.id.item_event_letter)
        val title: TextView = view.findViewById(R.id.item_event_title)
        val type: TextView = view.findViewById(R.id.item_event_type)
        val image: ImageView = view.findViewById(R.id.item_image)
        val location: TextView = view.findViewById(R.id.item_event_location)
        val date: TextView = view.findViewById(R.id.item_event_date)
        val description: TextView = view.findViewById(R.id.item_event_description)

    }

    init {
        model.events.observeForever { events ->
            this.events = events
            notifyDataSetChanged() // Notify adapter when dataset changes
        }
    }
    override fun getCount(): Int {
        return events.size
    }

    override fun getItem(position: Int): Any {
        return events[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)

        populateViewHolder(viewHolder, getItem(position) as Event)

        view.tag = viewHolder
        return view
    }

    private fun populateViewHolder(viewHolder: ViewHolder, event: Event) {
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
        }
    }
}