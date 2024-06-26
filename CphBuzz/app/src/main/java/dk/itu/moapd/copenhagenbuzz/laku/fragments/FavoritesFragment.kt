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

package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.laku.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.laku.adapters.FavoriteAdapter
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.laku.interfaces.FavoriteBtnListener
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.repositories.EventRepository

/**
 * Fragment that holds recyclerview for favorited events.
 */
class FavoritesFragment : Fragment(), FavoriteBtnListener {
    private var _binding: FragmentFavoritesBinding? = null
    private lateinit var _repo: EventRepository
    private lateinit var adapter: FavoriteAdapter
    private val db = Firebase.database(DATABASE_URL).reference

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    /**
     * Initialises view when it is created
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *  down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     * @param inflater Used to inflate the fragment
     **/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentFavoritesBinding.inflate(
            inflater,
            container,
            false
        ).also {
            _binding = it
        }.root

    /**
     * Sets up adapter for populating the fragment with events.
     *
     * @param view The view that was just created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _repo = EventRepository()

        FirebaseAuth.getInstance().currentUser?.let { user ->
            val query = db
                .child("copenhagen_buzz")
                .child("favorites")
                .child(user.uid)

            val options = FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(query, Event::class.java)
                .setLifecycleOwner(this)
                .build()

            adapter = FavoriteAdapter(options, this)

            with(binding) {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter

                val isValidUser = user != null && !user.isAnonymous

                if (isValidUser) {
                    notLoggedIn.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.GONE
                    notLoggedIn.visibility = View.VISIBLE
                }
            }
        }

    }

    /**
     * Implements interface function, listener for favorite button.
     * @param adapter The adapter connected to the listener
     * @param position Current index position in the viewholder
     */
    override fun onFavoriteBtnClicked(adapter: FavoriteAdapter, position: Int) {
        adapter.getRef(position).removeValue()
    }
}














