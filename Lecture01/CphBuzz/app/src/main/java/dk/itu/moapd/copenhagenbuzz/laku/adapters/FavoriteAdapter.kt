package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FavoriteRowItemBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.laku.models.Event

@SuppressLint("NotifyDataSetChanged")

class FavoriteAdapter(
    private val _model: DataViewModel
): RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    private var favorites: List<Event> = emptyList()

    companion object {
        private val TAG = FavoriteAdapter::class.qualifiedName
    }
    class ViewHolder(
        private val binding: FavoriteRowItemBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(event: Event) {
            with(binding) {
                Picasso.get().load(event.eventImage).into(eventImage)
                eventTitle.text = event.eventName
                eventType.text = event.eventType.toString()
            }
        }
    }

    init {
        _model.favorites.observeForever { favorites ->
            this.favorites = favorites
            notifyDataSetChanged() // Notify adapter when dataset changes
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = FavoriteRowItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
        .let(::ViewHolder)

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        Log.d(TAG, "Populate an item at position: $position")
        // Bind the view holder with the selected `DummyModel` data.
        favorites[position].let(holder::bind)
    }

    override fun getItemCount() = favorites.size

}