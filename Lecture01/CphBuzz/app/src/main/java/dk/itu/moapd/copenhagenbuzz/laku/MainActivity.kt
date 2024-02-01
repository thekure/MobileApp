package dk.itu.moapd.copenhagenbuzz.laku

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.view.WindowCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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

    // GUI variables
    private lateinit var eventName: EditText
    private lateinit var eventLocation: EditText
    private lateinit var eventDate: EditText
    private lateinit var eventType: AutoCompleteTextView
    private lateinit var eventDescription: EditText
    private lateinit var addEventButton: FloatingActionButton

    // An instance of the 'Event' class.
    private val event: Event = Event("","", "", "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window,false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Link the UI components with the Kotlin source-code
        eventName        = findViewById(R.id.edit_text_event_name)
        eventLocation    = findViewById(R.id.edit_text_event_location)
        eventDate        = findViewById(R.id.edit_text_event_date)
        eventType        = findViewById(R.id.auto_complete_event_types)
        eventDescription = findViewById(R.id.edit_text_event_description)
        addEventButton   = findViewById(R.id.fab_add_event)


        val eventTypes = resources.getStringArray(R.array.event_types)
        val arrayAdapter = ArrayAdapter(this, R.layout.event_type_dropdown, eventTypes)
        eventType.setAdapter(arrayAdapter)

        eventDate.setOnClickListener {
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
                eventDate.setText(combinedString)
            }
        }

        // Listener for user interaction in the 'Add Event' button
        addEventButton.setOnClickListener {
            // Only execute the following code when the user fills all 'EditText'
            if(eventName.text.toString().isNotEmpty() &&
                eventLocation.text.toString().isNotEmpty()) {

                // Update the object attributes
                event.setEventName(
                    eventName.text.toString().trim()
                )
                event.setEventLocation(
                    eventLocation.text.toString().trim()
                )
                event.setEventDate(
                    eventDate.text.toString().trim()
                )

                event.setEventType(
                    eventType.text.toString().trim()
                )

                event.setEventDescription(
                    eventDescription.text.toString().trim()
                )

                // Write in the 'Logcat' system
                showMessage()
            }
        }

        setContentView(binding.root)
    }

    private fun showMessage() {
        Log.d(TAG, event.toString())
    }
}















