package dk.itu.moapd.copenhagenbuzz.laku.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FavoriteRowItemBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import com.squareup.picasso.Picasso

@SuppressLint("NotifyDataSetChanged")

class FavoriteAdapter(
    private val data: List<Event>,
    private val favoritedListener: (Int) -> Unit,
    private val user: FirebaseUser?
): RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    companion object {
        private val TAG = FavoriteAdapter::class.qualifiedName
    }
    class ViewHolder(
        private val binding: FavoriteRowItemBinding,
        private val favoritedListener: (Int) -> Unit
    ): RecyclerView.ViewHolder(binding.root){
        init{
            binding.faveBtnFavorite.setOnClickListener {
                favoritedListener.invoke(adapterPosition)
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
    ): ViewHolder = FavoriteRowItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
        .let{binding -> ViewHolder(binding, favoritedListener)}

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        Log.d(TAG, "Populate an item at position: $position")
        // Bind the view holder with the selected `DummyModel` data.
        data[position].let(holder::bind)
    }

    override fun getItemCount() = data.size

}