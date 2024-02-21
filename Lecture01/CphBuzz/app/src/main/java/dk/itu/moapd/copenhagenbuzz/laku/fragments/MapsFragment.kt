package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentMapsBinding

class MapsFragment : Fragment() {
    private var _binding: FragmentMapsBinding? = null

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}