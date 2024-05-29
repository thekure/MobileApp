package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.copenhagenbuzz.laku.BUCKET_URL
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.databinding.DialogCreateEventBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.repositories.EventRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Handles dialog fragments for both "Create Event" and "Edit Event" requests.
 */
class CreateEventDialogFragment : DialogFragment() {

    private var _binding: DialogCreateEventBinding? = null
    private var startDateFromSelection: Long? = null
    private var endDateFromSelection:   Long? = null
    private var imageUri: Uri? = null
    private lateinit var downloadUrl: Uri
    private lateinit var storage: StorageReference
    private lateinit var _repo: EventRepository
    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        _binding = DialogCreateEventBinding.inflate(layoutInflater)
        _repo = EventRepository()

        if (!checkPermission()){
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setupEventTypeDropdown()
        setDatePickerListener()
        setupImageButtons()
        storage = Firebase.storage(BUCKET_URL).reference

        return buildCreateEventDialog()
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
            editTextEventLatitude.setText("55.659879")
            editTextEventLongitude.setText("12.59149")
        }
        // Remove above binding block


        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_event)
            .setView(binding.root)
            .setPositiveButton(R.string.create, null)
            .setNeutralButton("Cancel") { dialog, _ ->
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
                    userID = FirebaseAuth.getInstance().currentUser?.uid,
                    title = editTextEventName.text.toString().trim(),
                    location = editTextEventLocation.text.toString().trim(),
                    startDate = startDateFromSelection,
                    endDate = endDateFromSelection,
                    dateString = getDateString(startDateFromSelection, endDateFromSelection),
                    typeString = autoCompleteEventTypes.text.toString(),
                    description = editTextEventDescription.text.toString().trim(),
                    mainImage = downloadUrl.toString(),
                    latitude = editTextEventLatitude.text.toString().trim().toDouble(),
                    longitude = editTextEventLongitude.text.toString().trim().toDouble()
                )

                event.type = getTypeIndex(event.typeString!!)

                Log.d("Tag: CREATE EVENT DIALOG", "Event with userID ${event.userID} and title ${event.title} ready to be created.")
                _repo.createEvent(event)
                Log.d("Tag: CREATE EVENT DIALOG", "Return from repo create call.")
                hideKeyboard()
                showToast("Event created.")
                return true
            } else {
                Log.d("Tag: CREATE EVENT DIALOG", "Input not valid")
                showToast("Fill out all fields. Have you tried scrolling?")
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
            editTextEventImage.text.toString().isNotEmpty()         &&
            editTextEventLatitude.text.toString().isNotEmpty()      &&
            editTextEventLongitude.text.toString().isNotEmpty()
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
        if(startDate == null && endDate == null) return "No date"
        /**
         * Defines the wanted display format for the dates.
         * Currently set to: EEE, MMM dd yyyy.
         */
        val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.ENGLISH)
        var startDateAsString = ""
        var endDateAsString = ""
        if(startDate != null) startDateAsString = dateFormat.format(Date(startDate))
        if(endDate != null) endDateAsString = dateFormat.format(Date(endDate))


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



    companion object {
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if(didTakePhoto && imageUri != null) {
            Log.d("Tag: CAM", "takePhoto")
            uploadImageToBucket(imageUri!!)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun uploadImageToBucket(uri: Uri){
        val name = generateUniqueName()
        Log.d("Tag: CAM", "Generated image name: $name")
        storage.child(name).putFile(uri)
            .addOnSuccessListener {
                storage.child(name).downloadUrl
                    .addOnSuccessListener {
                        Log.d("Tag: CAM", "Saved downloadUrl as $it")
                        downloadUrl = it
                        binding.editTextEventImage.setText("Your image was uploaded.")
                    }
            }
    }

    private fun setupImageButtons() {
        Log.d("Tag: CAM", "setupButtons")

        binding.uploadBtn.setOnClickListener {
            Log.d("Tag: CAM", "uploadOnClickListener")
            pickPhotoFromGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.takePicBtn.setOnClickListener {
            if (checkPermission()){
                try{
                    val photoName = "IMG_${SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(Date())}.JPG"
                    val photoFile =
                        File(requireContext().applicationContext.filesDir, photoName)
                    Log.d("Tag: CAM", "Creating image file at ${photoFile.absolutePath}")
                    imageUri =
                        FileProvider.getUriForFile(
                            requireContext(),
                            "dk.itu.moapd.copenhagenbuzz.laku.fileprovider",
                            photoFile
                        )

                    Log.d("Tag: CAM", "takePhoto.launch(${imageUri})")
                    takePhoto.launch(imageUri)
                }
                 catch (e: Exception){
                     Log.d("Tag: CAM", "Encountered exception: $e")
                     e.printStackTrace()
                }
            }

            else{
                Log.d("Tag: CAM", "No permission")
            }
        }
    }

    private val pickPhotoFromGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { photoUri ->
        photoUri?.let {
            uploadImageToBucket(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun generateUniqueName(): String {
        return UUID.randomUUID().toString()
    }
}