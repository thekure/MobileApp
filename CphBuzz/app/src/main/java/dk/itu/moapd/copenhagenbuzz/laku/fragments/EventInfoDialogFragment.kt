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


import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dk.itu.moapd.copenhagenbuzz.laku.R
import dk.itu.moapd.copenhagenbuzz.laku.databinding.DialogEventInfoBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.utilities.WeatherUtil

class EventInfoDialogFragment(
    private val event: Event
) : DialogFragment() {

    private var _binding: DialogEventInfoBinding? = null

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    /**
     * Initialises dialog when it is created
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *      * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     *      */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        // Inflate the view using view binding.
        _binding = DialogEventInfoBinding.inflate(layoutInflater)

        // Populate the dialog view with user information.
        with(binding){
            with(event){
                mainImage.let { url ->
                    Picasso.get().load(url).into(imageViewPhoto)
                }
                editTextEventName.setText(title)
                editTextEventLocation.setText(location)
                editTextEventDate.setText(dateString)
                editTextEventType.setText(typeString)
                editTextEventDescription.setText(description)

                getWeather(event.latitude, event.longitude)

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

    /**
     * Nullifies binding on view destruction.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Method to call OpenMeteo Weather API with coordinate info.
     * Used to populate weather field in the event info dialog.
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     */
    private fun getWeather(lat: Double?, lon: Double?){
        if (lat != null && lon != null) {
            WeatherUtil.OpenMeteoApi.getCurrentTemperature(lat, lon) { temp ->
                activity?.runOnUiThread {
                    binding.textFieldEventWeather.text = temp?.let {
                        getString(R.string.temperature_text, it)
                    } ?: getString(R.string.temperature_unavailable)
                }
            }
        } else {
            binding.textFieldEventWeather.text = getString(R.string.temperature_unavailable)
        }
    }
}

