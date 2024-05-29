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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.laku.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.adapters.EventAdapter
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentTimelineBinding
import dk.itu.moapd.copenhagenbuzz.laku.interfaces.EventBtnListener
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.repositories.EventRepository

class TimelineFragment : Fragment(), EventBtnListener {
    private var _binding: FragmentTimelineBinding? = null
    private lateinit var _repo: EventRepository
    private val db = Firebase.database(DATABASE_URL).reference
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


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
        _repo = EventRepository()

        Log.d("Tag: TIMELINE FRAGMENT", "Setting query. db ref: $db")

        val query = db
            .child("copenhagen_buzz")
            .child("events")
            .orderByChild("startDate")

        Log.d("Tag: TIMELINE FRAGMENT", "Setting query. Full query: $db.copenhagen_buzz.events" )

        val options = FirebaseListOptions.Builder<Event>()
            .setLifecycleOwner(this)
            .setLayout(R.layout.event_row_item)
            .setQuery(query, Event::class.java)
            .build()

        val adapter = EventAdapter(
            options = options,
            context = context,
            repository = _repo,
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            user = FirebaseAuth.getInstance().currentUser,
            onClickListener = this@TimelineFragment
            )

        adapter.setFaveCallback {  }
        binding.listViewTimeline.adapter = adapter
    }

    /**
     * Interface method implementation of editBtn listener
     */
    override fun onEditEventClicked(event: Event, position: Int) {
        EditEventDialogFragment(event).apply {
            isCancelable = true
        }.also { dialogFragment ->
            dialogFragment.show(childFragmentManager, "CreateEventDialogFragment")
        }
    }

    /**
     * Interface method implementation of infoBtn listener
     */
    override fun onInfoEventClicked(event: Event, position: Int) {
        EventInfoDialogFragment(event).apply {
            isCancelable = true
        }.also { dialogFragment ->
            dialogFragment.show(childFragmentManager, "EventInfoDialogFragment")
        }
    }
}













