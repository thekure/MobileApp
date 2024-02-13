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

package dk.itu.moapd.copenhagenbuzz.laku

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.laku.databinding.ActivityMainBinding
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * An activity class with methods to manage the main activity of the application.
 */
class MainActivity : AppCompatActivity() {

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     * - This text was written by Fabricio Narcizo
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * A set of private constants used in this class.
     * - This text was written by Fabricio Narcizo
     */
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    // An instance of the 'Event' class.
    private lateinit var event: Event

    /**
     * Called when the activity is starting. This is where most initialization should go: calling
     * `setContentView(int)` to inflate the activity's UI, using `findViewById()` to
     * programmatically interact with widgets in the UI, calling
     * `managedQuery(android.net.Uri, String[], String, String[], String)` to retrieve cursors for
     * data being displayed, etc.
     *
     * You can call `finish()` from within this function, in which case `onDestroy()` will be
     * immediately called after `onCreate()` without any of the rest of the activity lifecycle
     * (`onStart()`, `onResume()`, onPause()`, etc) executing.
     *
     * <em>Derived classes must call through to the super class's implementation of this method. If
     * they do not, an exception will be thrown.</em>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     * <b><i>Note: Otherwise it is null.</i></b>
     * - This text was written by Fabricio Narcizo
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window,false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        eventTypeSetup()
        setListeners()

    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        menu.findItem(R.id.accounts_button).isVisible =
        !intent.getBooleanExtra("isLoggedIn", false)
        menu.findItem(R.id.logout_button).isVisible =
        intent.getBooleanExtra("isLoggedIn", false)
        return true
    }


    /**
     * Sets up the user interface components by attaching relevant listeners to the
     * necessary components. These listeners have dedicated handler functions.
     */
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

        with(binding.topAppBar){
            setOnMenuItemClickListener{menuItem ->
                when (menuItem.itemId) {
                    R.id.accounts_button -> {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }

                    R.id.logout_button -> {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }

                    else -> false
                }
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

        datePicker.show(supportFragmentManager, "tag")

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
            val combinedString = String.format(getString(R.string.date_range), startDate, endDate) //"@strings/date_range" //""$startDate - $endDate"
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
        val arrayAdapter = ArrayAdapter(this, R.layout.event_type_dropdown, eventTypes)

        binding.contentMain.autoCompleteEventTypes.setAdapter(arrayAdapter)
    }

    /**
     * Handler function for the add event button.
     * - Creates new event if all fields have content.
     * - Notifies user with a snack bar
     * - If fields are filled incorrectly, notifies user with a toast
     */
    private fun handleAddEventAction(view: View){
        with(binding.contentMain) {
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
                hideKeyboard()
            } else {
                Toast.makeText(this@MainActivity, "You need to fill out all fields first.", Toast.LENGTH_SHORT).show()
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
        with(binding.contentMain){
                editTextEventName.text.toString().isNotEmpty() &&
                editTextEventLocation.text.toString().isNotEmpty() &&
                editTextEventDate.text.toString().isNotEmpty() &&
                autoCompleteEventTypes.text.toString().isNotEmpty() &&
                editTextEventDescription.text.toString().isNotEmpty()
        }

    /**
     * Standard function for hiding the keyboard. Imported from java.
     */
    private fun hideKeyboard(){
        val imm = getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}















