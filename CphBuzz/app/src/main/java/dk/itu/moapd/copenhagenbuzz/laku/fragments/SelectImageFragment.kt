package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.itu.moapd.copenhagenbuzz.laku.R

import android.app.Activity
import android.content.Intent
import android.net.Uri

class ImageSelectFragment : Fragment() {

    companion object {
        private const val REQUEST_SELECT_IMAGE = 1001
    }

    /**
     * Create the fragment view
     * @param inflater can be used to inflate any views in the fragment.
     * @param container is the view that the fragment's UI should be attached to.
     * @param savedInstanceState of the fragment.
     * @return the root view of the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectImage()
    }

    /**
     * Opens the image picker to select an image locally from the device
     */
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    interface OnPhotoChosenListener {
        fun onPhotoTaken(imageUri: Uri)
    }

    // Declare the callback variable
    private var photoChosenListener: OnPhotoChosenListener? = null

    // Function to set the callback listener
    fun setOnPhotoChosenListener(listener: OnPhotoChosenListener) {
        photoChosenListener = listener
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->

                photoChosenListener?.onPhotoTaken(imageUri)
            }
        }
    }
}