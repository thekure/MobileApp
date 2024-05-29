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

class MapsFragment : Fragment() {
    private var _binding: FragmentMapsBinding? = null

    private val binding
        get() = requireNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private var locationService: LocationService? = null
    companion object {
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    override fun onStart() {
        super.onStart()

        // Bind to the service.
        Intent(requireContext(), LocationService::class.java).let { serviceIntent ->
            requireActivity().bindService(
                serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

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

    private val serviceConnection = object : ServiceConnection {

        /**
         * Called when a connection to the Service has been established, with the
         * `android.os.IBinder` of the communication channel to the Service.
         *
         * If the system has started to bind your client app to a service, it's possible that your
         * app will never receive this callback. Your app won't receive a callback if there's an
         * issue with the service, such as the service crashing while being created.
         *
         * @param name The concrete component name of the service that has been connected.
         * @param service The IBinder of the Service's communication channel, which you can now make
         *      calls on.
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

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

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

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
    }

    override fun onResume() {
        super.onResume()

        // Register the broadcast receiver.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

    private fun updateLocationDetails(location: Location) {
        with(binding) {
            editTextLatitude.setText(location.latitude.toString())
            editTextLongitude.setText(location.longitude.toString())
            editTextAltitude.setText(location.altitude.toString())
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
}
