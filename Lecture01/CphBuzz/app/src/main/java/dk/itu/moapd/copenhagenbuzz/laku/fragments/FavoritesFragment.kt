package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dk.itu.moapd.copenhagenbuzz.laku.adapters.FavoriteAdapter
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentFavoritesBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val _model: DataViewModel by viewModels()

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

        val adapter = FavoriteAdapter(_model)
            with(binding) {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter

        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}














