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

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.laku.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.laku.databinding.FragmentMapsBinding
import dk.itu.moapd.copenhagenbuzz.laku.models.Event
import dk.itu.moapd.copenhagenbuzz.laku.services.LocationService

/**
 * Fragment that holds Google Maps implementation
 */
class MapsFragment : Fragment() {
    private var _binding: FragmentMapsBinding? = null
    private var locationService: LocationService? = null
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    companion object {
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }

    /**
     * Creates intent to bind location service to the fragment.
     */
    override fun onStart() {
        super.onStart()

        // Bind to the service.
        Intent(requireContext(), LocationService::class.java).let { serviceIntent ->
            requireActivity().bindService(
                serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Inflates view after creation
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    /**
     * Binds fragment to map
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationBroadcastReceiver = LocationBroadcastReceiver()

        val mapFragment = childFragmentManager
            .findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // Bind to the service.
        Intent(requireContext(), LocationService::class.java).let { serviceIntent ->
            requireActivity().bindService(
                serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Makes sure service is unregistered on pause.
     */
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
    }

    /**
     * Makes sure service is registered again when needed.
     */
    override fun onResume() {
        super.onResume()

        // Register the broadcast receiver.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    /**
     * Nullifies binding on view destruction
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Creates a prompt for the user to give permission
     */
    private fun requestUserPermissions() {
        if (!checkPermission())
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
    }

    /**
     * Checks permissions
     */
    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED



    private val serviceConnection = object : ServiceConnection {

        /**
         * When a connection has been made to the service, this is called.
         * If there is an issue, the app might not receive a callback.
         *
         * @param name Component name of the connected service.
         * @param service Service IBinder to make calls on.
         */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service

            if (checkPermission()) {
                locationService?.subscribeToLocationUpdates()
            } else {
                requestUserPermissions()
            }
        }

        /**
         * Disconnect callback
         */
        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
        }
    }



    /**
     * Receives broadcast location updates from the service.
     */
    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context, intent: Intent) {
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION,
                    Location::class.java)
            else
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION)
            location?.let {
                updateLocationDetails(it)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        // Add a marker in IT University of Copenhagen and move the camera.
        val itu = LatLng(55.6596, 12.5910)
        googleMap.addMarker(MarkerOptions().position(itu).title("IT University of Copenhagen"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(itu))

        val query = Firebase.database(DATABASE_URL).reference
            .child("copenhagen_buzz")
            .child("events")
            .orderByChild("eventStart")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (eventSnapshot in dataSnapshot.children) {
                    val longitude = eventSnapshot.child("longitude").getValue(Double::class.java)
                    val latitude = eventSnapshot.child("latitude").getValue(Double::class.java)
                    val name = eventSnapshot.child("title").getValue(String::class.java)

                    if (longitude != null && latitude != null) {
                        val thislocation = LatLng(latitude, longitude)
                        googleMap.addMarker(MarkerOptions().position(thislocation).title(name))
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                println("Error: ${databaseError.message}")
            }
        })

        // Move the Google Maps UI buttons under the OS top bar.
        googleMap.setPadding(0, 100, 0, 0)

        // Enable the location layer. Request the permission if it is not granted.
        if (checkPermission()) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestUserPermissions()
        }
    }

    /**
     * Keeps invisible text fields on the map updated.
     */
    private fun updateLocationDetails(location: Location) {
        with(binding) {
            editTextLatitude.setText(location.latitude.toString())
            editTextLongitude.setText(location.longitude.toString())
            editTextAltitude.setText(location.altitude.toString())
        }
    }
}
