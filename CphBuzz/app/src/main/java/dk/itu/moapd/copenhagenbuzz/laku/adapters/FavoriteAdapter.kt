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

/**
 * This class is responsible for populating the individual event objects into the
 * viewholder. It extends FirebaseListAdapter which keeps the data up-to-date.
 *
 * @param options These are the FirebaseRecycler options necessary for proper db communication
 * @param onFavoriteBtnClicked An interface with listener function for the favorite button
 */
class FavoriteAdapter(
    options: FirebaseRecyclerOptions<Event>,
    private val onFavoriteBtnClicked: FavoriteBtnListener
): FirebaseRecyclerAdapter<Event, FavoriteAdapter.ViewHolder>(options) {

    /**
     * Object that holds all views
     * @param binding FavoriteRowItemBinding
     * @param adapter The adapter that manages each row item
     * @param onFavoriteBtnClicked Listener for the favorite button
     */
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

    /**
     * Initialises viewholder when it is created
     * @param parent The parent view group
     * @param viewType An integer
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        FavoriteRowItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
        .let{binding -> ViewHolder(binding, this, onFavoriteBtnClicked)}

    /**
     * @param holder Viewholder
     * @param position Indexed position where event needs to be bound
     * @param event Event to bind
     */
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        event: Event
    ) {
        Log.d("Tag: FAVORITE ADAPTER", "Populate an item at position: $position")
        event.let(holder::bind)
    }
}