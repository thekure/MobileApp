package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FavoriteRowItemBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.interfaces.FavoriteBtnListener
import dk.itu.moapd.copenhagenbuzz.laku.repositories.EventRepository

class FavoriteAdapter(
    options: FirebaseRecyclerOptions<Event>,
    private val onFavoriteBtnClicked: FavoriteBtnListener
): FirebaseRecyclerAdapter<Event, FavoriteAdapter.ViewHolder>(options) {

    class ViewHolder(
        private val binding: FavoriteRowItemBinding,
        private val adapter: FavoriteAdapter,
        private val onFavoriteBtnClicked: FavoriteBtnListener
    ): RecyclerView.ViewHolder(binding.root){
        init{
            binding.faveBtnFavorite.setOnClickListener {
                onFavoriteBtnClicked.onFavoriteBtnClicked(adapter, absoluteAdapterPosition)
            }
        }
        fun bind(event: Event) {
            with(binding) {
                Picasso.get().load(event.mainImage).into(faveEventImage)
                faveEventTitle.text = event.title
                faveEventType.text = event.typeString
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        FavoriteRowItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
        .let{binding -> ViewHolder(binding, this, onFavoriteBtnClicked)}

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        event: Event
    ) {
        Log.d("Tag: FAVORITE ADAPTER", "Populate an item at position: $position")
        event.let(holder::bind)
    }
}