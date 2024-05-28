package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.laku.interfaces.EventFavoriteStatusProvider
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
        val favoritedStatusProvider = EventFavoriteStatusProvider(_model)
        _model.events.observe(viewLifecycleOwner){ events ->
            val adapter = EventAdapter(
                context = requireContext(),
                resource = R.layout.event_row_item,
                data = events,
                onEditEventClicked = this::showEditEventDialog,
                onEventInfoClicked = this::showEventInfoDialog,
                onDeleteEventClicked = this::deleteEventClicked,
                user = _model.getUser(),
                favoritedStatusProvider = favoritedStatusProvider
            )
            adapter.refreshData(events)
            binding.listViewTimeline.adapter = adapter
        }
    }

    private fun showEditEventDialog(position: Int) {
        CreateEventDialogFragment(true, position).apply {
            isCancelable = true
        }.also { dialogFragment ->
            dialogFragment.show(childFragmentManager, "CreateEventDialogFragment")
        }
    }

    private fun showEventInfoDialog(position: Int) {
        EventInfoDialogFragment(position).apply {
            isCancelable = true
        }.also { dialogFragment ->
            dialogFragment.show(childFragmentManager, "EventInfoDialogFragment")
        }
    }

    private fun deleteEventClicked(position: Int){
        val favoritedStatusProvider = EventFavoriteStatusProvider(_model)
        val event = _model.getEventAtIndex(position)
        if(favoritedStatusProvider.isEventFavorited(event)) {
            _model.removeFromFavorites(event)
        }
        _model.deleteEvent(event)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}













