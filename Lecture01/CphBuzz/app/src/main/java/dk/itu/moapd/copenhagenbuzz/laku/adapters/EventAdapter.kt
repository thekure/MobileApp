package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.github.javafaker.Faker
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.models.EventType

class EventAdapter(private val context: Context, private var resource: Int,
                   data: List<Event>): BaseAdapter() {

   private val dummy: List<Event> = generateDummyEvents()

    private class ViewHolder(view: View){
        val eventLetter: ImageView = view.findViewById(R.id.item_event_letter)
        val title: TextView = view.findViewById(R.id.item_event_title)
        val type: TextView = view.findViewById(R.id.item_event_type)
        val image: ImageView = view.findViewById(R.id.item_image)
        val location: TextView = view.findViewById(R.id.item_event_location)
        val date: TextView = view.findViewById(R.id.item_event_date)
        val description: TextView = view.findViewById(R.id.item_event_description)

    }
    override fun getCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getItem(position: Int): Any {
        return dummy[position]
    }

    override fun getItemId(position: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val viewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)

        getItem(position)?.let { dummy ->
            populateViewHolder(viewHolder, dummy as Event)
        }

        view.tag = viewHolder
        return view
    }

    private fun populateViewHolder(viewHolder: ViewHolder, dummy: Event) {
        with(viewHolder) {
            // Fill out the Material Design card.
            Picasso.get().load("https://picsum.photos/300/200").into(image)
            title.text = dummy.eventName
            type.text = dummy.eventType.toString()
            description.text = dummy.eventDescription
            Picasso.get().load("https://picsum.photos/100/100").into(eventLetter)
            location.text = dummy.eventLocation
            date.text = dummy.eventDate
        }
    }

    // Dummy function to generate a list of events (replace this with your actual data source)
    private fun generateDummyEvents(): List<Event> {
        // Generate dummy events here
        val faker = Faker()
        val eventList = mutableListOf<Event>()
        repeat(10) {
            val event = Event(
                eventName = faker.lorem().word(),
                eventLocation = faker.address().city(),
                eventDate = faker.date().toString(),
                eventType = EventType.WEDDING,
                eventDescription = faker.lorem().word()
            )
            eventList.add(event)
        }
        return eventList
    }
}