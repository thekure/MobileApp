package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel

class TimelineFragment : Fragment() {
    private var _binding: FragmentTimelineBinding? = null

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var _model: DataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentTimelineBinding.inflate(
            inflater,
            container,
            false
        ).also {
            _binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _model = ViewModelProvider(requireActivity())[DataViewModel::class.java]
        _model.events.observe(viewLifecycleOwner){ events ->
            val adapter = EventAdapter(
                requireContext(),
                R.layout.event_row_item,
                events,
                favoritedListener = { position ->
                    val event = _model.events.value?.get(position)
                    _model.invertIsFavorited(event!!)
                }
            )

            binding.listViewTimeline.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}












