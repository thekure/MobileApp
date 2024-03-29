package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.databinding.DialogCreateEventBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles dialog fragments for both "Create Event" and "Edit Event" requests.
 */
class CreateEventDialogFragment(
    private val isEdit: Boolean = false,
    private val position: Int = -1
) : DialogFragment() {

    private var _binding: DialogCreateEventBinding? = null
    private lateinit var model: DataViewModel
    private var startDateFromSelection: Long? = null
    private var endDateFromSelection:   Long? = null
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        _binding = DialogCreateEventBinding.inflate(layoutInflater)
        model = ViewModelProvider(requireActivity())[DataViewModel::class.java]

        setupEventTypeDropdown()
        setDatePickerListener()

        // Return appropriate dialog variant
        return if(isEdit) buildEditEventDialog()
        else              buildCreateEventDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Create and return a fresh create event dialog.
     */
    private fun buildCreateEventDialog(): androidx.appcompat.app.AlertDialog{
        // Remove this when done testing
        with(binding){
            editTextEventName.setText("TestEvent")
            editTextEventLocation.setText("TestLocation")
            editTextEventDate.setText("Wed, May 15 2024")
            editTextEventDescription.setText("Test Description")
            editTextEventImage.setText("https://picsum.photos/seed/290/400/194")
        }
        // Remove above binding block

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_event)
            .setView(binding.root)
            .setPositiveButton(R.string.create, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if(createEventFromFields()) dismiss()
                    }
                }
            }
    }

    /**
     * Handler function for the add event button.
     * - Creates new event if all fields have content.
     * - Notifies user with a snack bar
     * - If fields are filled incorrectly, notifies user with a toast
     */
    private fun createEventFromFields(): Boolean{
        with(binding) {
            // Only execute the following code when the user fills all fields
            if (checkInputValidity()) {
                val event = Event(
                    userID = model.getUser()!!.uid,
                    title = editTextEventName.text.toString().trim(),
                    location = editTextEventLocation.text.toString().trim(),
                    startDate = startDateFromSelection,
                    endDate = endDateFromSelection,
                    dateString = getDateString(startDateFromSelection, endDateFromSelection),
                    typeString = autoCompleteEventTypes.text.toString(),
                    description = editTextEventDescription.text.toString().trim(),
                    mainImage = editTextEventImage.text.toString().trim(),
                )

                event.type = getTypeIndex(event.typeString!!)

                model.createEvent(event)
                hideKeyboard()
                showToast("Event created.")
                return true
            } else {
                showToast("You need to fill out all fields first.")
                return false
            }
        }
    }

    /**
     * Create and return an edit event dialog.
     */
    private fun buildEditEventDialog(): androidx.appcompat.app.AlertDialog {
        val event = model.getEventAtIndex(position)

        with(binding){
            editTextEventName.setText(event.title)
            editTextEventLocation.setText(event.location)
            editTextEventDate.setText(event.dateString)
            autoCompleteEventTypes.setText(event.typeString, false)
            editTextEventDescription.setText(event.description)
            editTextEventImage.setText(event.mainImage)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_event)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if(editEventFromFields(event)) dismiss()
                    }
                }
            }
    }

    /**
     * Handler function for the edit event button.
     * - Edits existing event if all fields have content.
     * - Notifies user with a snack bar
     * - If fields are filled incorrectly, notifies user with a toast
     */
    private fun editEventFromFields(event: Event): Boolean{
        with(binding) {
            // Only execute the following code when the user fills all fields
            if (checkInputValidity()) {
                event.title = editTextEventName.text.toString().trim()
                event.location = editTextEventLocation.text.toString().trim()
                event.startDate = startDateFromSelection
                event.endDate = endDateFromSelection
                event.dateString = getDateString(startDateFromSelection, endDateFromSelection)
                event.typeString = autoCompleteEventTypes.text.toString()
                event.description = editTextEventDescription.text.toString().trim()
                event.mainImage = editTextEventImage.text.toString().trim()
                event.type = getTypeIndex(event.typeString!!)

                model.updateEvent(event)
                hideKeyboard()
                showToast("Event updated.")
                return true
            } else {
                showToast("You need to fill out all fields first.")
                return false
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

            startDateFromSelection = selection.first
            endDateFromSelection = selection.second

            with(binding.editTextEventDate){
                setText(getDateString(selection.first, selection.second))
                clearFocus()
            }
        }
    }

    /**
     * Sets up the user interface components by attaching relevant listeners to the
     * necessary components. These listeners have dedicated handler functions.
     */
    private fun setDatePickerListener() {
        binding.editTextEventDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) { handleDatePickerAction() }
        }
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
    private fun setupEventTypeDropdown(){
        val eventTypes = resources.getStringArray(R.array.event_types)
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.event_type_dropdown,
            eventTypes
        )

        with(binding.autoCompleteEventTypes){
            setAdapter(arrayAdapter)
            setOnItemClickListener { _, _, _, _ ->
                hideKeyboard()
            }
        }
    }

    /**
     * Checks that all required fields have content before event creation.
     */
    private fun checkInputValidity(): Boolean =
        with(binding){
            editTextEventName.text.toString().isNotEmpty()          &&
            editTextEventLocation.text.toString().isNotEmpty()      &&
            editTextEventDate.text.toString().isNotEmpty()          &&
            autoCompleteEventTypes.text.toString().isNotEmpty()     &&
            editTextEventDescription.text.toString().isNotEmpty()   &&
            editTextEventImage.text.toString().isNotEmpty()
        }

    /**
     * Helper function that retrieves index of the given event type.
     * - Written to handle addition of new types automatically.
     */
    private fun getTypeIndex(type: String): Int{
        val eventTypeArray = resources.getStringArray(R.array.event_types)
        val eventTypeMap = mutableMapOf<String, Int>()

        for (i in eventTypeArray.indices) {
            val eventType = eventTypeArray[i]
            eventTypeMap[eventType] = i
        }

        return eventTypeMap[type]!!
    }

    /**
     * Helper function to convert dates from long to string.
     */
    private fun getDateString(startDate: Long?, endDate: Long?): String{
        if(startDate == null || endDate == null) return ""
        /**
         * Defines the wanted display format for the dates.
         * Currently set to: EEE, MMM dd yyyy.
         */
        val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH)
        val startDateAsString = dateFormat.format(Date(startDate))
        val endDateAsString = dateFormat.format(Date(endDate))

        if(startDateAsString == endDateAsString) return startDateAsString

        return "$startDateAsString - $endDateAsString"
    }

    private fun showToast(message: String){
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = requireActivity().currentFocus
        if (currentFocusedView != null) {
            imm.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
        }
    }
}