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

class FavoritesFragment : Fragment(), FavoriteBtnListener {
    private var _binding: FragmentFavoritesBinding? = null
    private lateinit var _repo: EventRepository
    private lateinit var adapter: FavoriteAdapter
    private val db = Firebase.database(DATABASE_URL).reference

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

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


    override fun onFavoriteBtnClicked(adapter: FavoriteAdapter, position: Int) {
        adapter.getRef(position).removeValue()
    }

    fun clearFavoriteAdapter() {
        with(binding) {
            recyclerView.adapter = null
        }
    }
}














