package dk.itu.moapd.copenhagenbuzz.laku.fragments

import android.Manifest
import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentCameraBinding
import java.util.Date
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var imageCapture: ImageCapture

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var imageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        cameraPermissionResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCameraBinding .inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Request camera permissions.
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        // Define the UI behavior.
        binding.apply {

            // Set up the listener for take photo button.
            buttonImageCapture.setOnClickListener {
                takePhoto()
            }
        }
    }

    /**
     * Sets up the camera preview and image capture
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                updateCameraSwitchButton(cameraProvider)
            } catch(ex: Exception) {
                //showSnackBar("Use case binding failed: $ex")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Take photo and save locally on device
     */
    private fun takePhoto() {
        val imageCapture = imageCapture
        val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())
        val filename = "IMG_$timestamp.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }
        val outputFileOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireActivity().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {

                /**
                 * Called when an image has been successfully saved.
                 */
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imageUri = output.savedUri
                    photoTakenListener?.onPhotoTaken(imageUri!!)
                    Log.d("TAG", imageUri.toString())
                }

                /**
                 * Called when an error occurs while attempting to save an image.
                 *
                 * @param exception An `ImageCaptureException` that contains the type of error, the
                 *      error message and the throwable that caused it.
                 */
                override fun onError(exception: ImageCaptureException) {
                }
            }
        )
    }

    /**
     * Activates camera switch button if ProcessCameraProvider has both front and back camera
     * @param provider device to check on
     */
    private fun updateCameraSwitchButton(provider: ProcessCameraProvider) {
        binding.buttonCameraSwitch.isEnabled = try {
            hasBackCamera(provider) && hasFrontCamera(provider)
        } catch (exception: CameraInfoUnavailableException) {
            false
        }
    }

    /**
     * Handles the camera permission
     * @param isGranted define permission
     */
    private fun cameraPermissionResult(isGranted: Boolean) {
        // Use the takeIf function to conditionally execute code based on the permission result
        isGranted.takeIf { it }?.run {
            startCamera()
        } ?: requireActivity().finish()
    }

    /**
     * Checks if device have back camera
     * @param provider device to check on
     * @return true if ProcessCameraProvider has a back camera
     */
    private fun hasBackCamera(provider: ProcessCameraProvider) =
        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

    /**
     * Checks if device have front camera
     * @param provider device to check on
     * @return true if ProcessCameraProvider has a front camera
     */
    private fun hasFrontCamera(provider: ProcessCameraProvider) =
        provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)

    interface OnPhotoTakenListener {
        fun onPhotoTaken(imageUri: Uri)
    }

    // Declare the callback variable
    private var photoTakenListener: OnPhotoTakenListener? = null

    /**
     * Set photo taken listener
     * @param listener to set as photoTakenListener
     */
    fun setOnPhotoTakenListener(listener: OnPhotoTakenListener) {
        photoTakenListener = listener
    }
}