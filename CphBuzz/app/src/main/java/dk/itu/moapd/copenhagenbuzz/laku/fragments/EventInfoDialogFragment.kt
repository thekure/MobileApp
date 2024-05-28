
package dk.itu.moapd.copenhagenbuzz.laku.fragments


import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.databinding.DialogEventInfoBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel

class EventInfoDialogFragment(
    private val position: Int = -1
) : DialogFragment() {

    private var _binding: DialogEventInfoBinding? = null
    private lateinit var model: DataViewModel

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        // Inflate the view using view binding.
        _binding = DialogEventInfoBinding.inflate(layoutInflater)
        model = ViewModelProvider(requireActivity())[DataViewModel::class.java]

        // Get the current event.
        val event = model.getEventAtIndex(position)

        // Populate the dialog view with user information.
        with(binding){
            with(event){
                mainImage.let { url ->
                    imageViewPhoto.imageTintMode = null
                    Picasso.get().load(url).into(imageViewPhoto)
                }
                editTextEventName.setText(title)
                editTextEventLocation.setText(location)
                editTextEventDate.setText(dateString)
                editTextEventType.setText(typeString)
                editTextEventDescription.setText(description)
            }

        }

        // Create and return a new dialog.
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.event_info_title)
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

