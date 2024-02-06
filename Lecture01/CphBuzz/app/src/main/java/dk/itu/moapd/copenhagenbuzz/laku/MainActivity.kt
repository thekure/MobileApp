package dk.itu.moapd.copenhagenbuzz.laku

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.laku.databinding.ActivityMainBinding
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // A set of private constants used in this class.
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    // An instance of the 'Event' class.
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window,false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventTypeSetup()
        setListeners()
        setContentView(binding.root)
    }

    private fun setListeners() {
        with(binding.contentMain) {
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

    private fun handleDatePickerAction(){
        val datePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .build()

        datePicker.show(supportFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH)
            val startDate = dateFormat.format(Date(selection.first))
            val endDate = dateFormat.format(Date(selection.second))
            val combinedString = "$startDate - $endDate"
            with(binding.contentMain.editTextEventDate){
                if(startDate == endDate){
                    setText(startDate)
                } else {
                    setText(combinedString)
                }
                clearFocus()
            }
        }
    }

    private fun eventTypeSetup(){
        val eventTypes = resources.getStringArray(R.array.event_types)
        val arrayAdapter = ArrayAdapter(this, R.layout.event_type_dropdown, eventTypes)

        binding.contentMain.autoCompleteEventTypes.setAdapter(arrayAdapter)
    }

    private fun handleAddEventAction(view: View){
        with(binding.contentMain) {
            // Only execute the following code when the user fills all 'EditText'
            if (checkInputValidity()) {
                event = Event(
                    editTextEventName.text.toString().trim(),
                    editTextEventLocation.text.toString().trim(),
                    editTextEventDate.text.toString().trim(),
                    enumValueOf(autoCompleteEventTypes.text.toString().uppercase()),
                    editTextEventDescription.text.toString().trim()
                )

                // Write in the 'Logcat' system
                showMessage(view)
                hideKeyboard()
            } else {
                Toast.makeText(this@MainActivity, "You need to fill out all fields first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMessage(view: View) {
        Snackbar.make(view, event.toString(), Snackbar.LENGTH_SHORT).show()
    }

    private fun checkInputValidity(): Boolean =
        with(binding.contentMain){
                editTextEventName.text.toString().isNotEmpty() &&
                editTextEventLocation.text.toString().isNotEmpty() &&
                editTextEventDate.text.toString().isNotEmpty() &&
                autoCompleteEventTypes.text.toString().isNotEmpty() &&
                editTextEventDescription.text.toString().isNotEmpty()
        }

    private fun hideKeyboard(){
        val imm = getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}















