package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentEventCreateBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventCreateFragment : Fragment() {
    private var _binding: FragmentEventCreateBinding? = null

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    // An instance of the 'Event' class.
    private lateinit var event: Event

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentEventCreateBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventTypeSetup()
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Sets the correct values for the dropdown component.
     * Event types are provided as a string array in strings.xml and are defined as enums in
     * Event.kt. The dropdown component is an AutoCompleteTextView, which has a setAdapter function
     * to specify the input of the dropdown values.
     * - Loads string array with the event types.
     * - Instantiates a correct adapter.
     * - Updates the component with the new adapter.
     */
    private fun eventTypeSetup(){
        val eventTypes = resources.getStringArray(R.array.event_types)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.event_type_dropdown, eventTypes)

        binding.autoCompleteEventTypes.setAdapter(arrayAdapter)
    }

    /**
     * Sets up the user interface components by attaching relevant listeners to the
     * necessary components. These listeners have dedicated handler functions.
     */
    private fun setListeners() {
        with(binding) {
            editTextEventDate.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus){
                    handleDatePickerAction()
                }
            }

            fabAddEvent.setOnClickListener {
                handleAddEventAction(it)
            }
        }
    }

    /**
     * Handler function for the date picker component.
     * Instantiates a date range picker, and sets a listener for positive button click in
     * the picker. User selection generates a pair of dates in utc millisecond format.
     * - Uses the SimpleDateFormat library to define the intended string format for the UI.
     * - Converts from utc milliseconds -> date -> string
     * - Sets the UI text to the date.
     * - Removes users focus from the component.
     */
    private fun handleDatePickerAction(){
        val datePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .build()
        datePicker.show(parentFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            /**
             * Defines the wanted display format for the dates.
             * Currently set to: EEE, MMM dd yyyy.
             */
            val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH)
            val startDate = dateFormat.format(Date(selection.first))
            val endDate = dateFormat.format(Date(selection.second))

            /**
             * Uses start- and end dates to generate a single range string, using the date
             * range resource, which takes 2 parameters.
             */
            val combinedString = "$startDate - $endDate" //"@strings/date_range" //""$startDate - $endDate"
            with(binding.editTextEventDate){
                if(startDate == endDate){
                    setText(startDate)
                } else {
                    setText(combinedString)
                }
                clearFocus()
            }
        }
    }

    /**
     * Handler function for the add event button.
     * - Creates new event if all fields have content.
     * - Notifies user with a snack bar
     * - If fields are filled incorrectly, notifies user with a toast
     */
    private fun handleAddEventAction(view: View){
        with(binding) {
            // Only execute the following code when the user fills all fields
            if (checkInputValidity()) {
                event = Event(
                    editTextEventName.text.toString().trim(),
                    editTextEventLocation.text.toString().trim(),
                    editTextEventDate.text.toString().trim(),
                    enumValueOf(autoCompleteEventTypes.text.toString().uppercase()),
                    editTextEventDescription.text.toString().trim()
                )

                // Show snack bar with event data.
                showMessage(view)
                //hideKeyboard()
            } else {
                Toast.makeText(requireContext(), "You need to fill out all fields first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Responsible for displaying event creation message on the UI.
     */
    private fun showMessage(view: View) {
        Snackbar.make(view, event.toString(), Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Checks that all required fields have content before event creation.
     */
    private fun checkInputValidity(): Boolean =
        with(binding){
            editTextEventName.text.toString().isNotEmpty() &&
                    editTextEventLocation.text.toString().isNotEmpty() &&
                    editTextEventDate.text.toString().isNotEmpty() &&
                    autoCompleteEventTypes.text.toString().isNotEmpty() &&
                    editTextEventDescription.text.toString().isNotEmpty()
        }


}