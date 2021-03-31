package com.t3ddyss.clother.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentLocationBinding
import com.t3ddyss.clother.ui.offer_editor.OfferEditorViewModel
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.MAPVIEW_BUNDLE


class LocationFragment : Fragment() {

    private val viewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private var mapView: MapView? = null
    private lateinit var map: GoogleMap
    private lateinit var locationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val openSettingsAction = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        val requestLocationPermissionsLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
                    if (isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true
                            && isGranted[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                        getLocation()
                    }
                    else {
                        (activity as? MainActivity)
                                ?.showSnackbarWithAction(
                                    message = getString(R.string.no_location_access),
                                    actionText = getString(R.string.grant_permission),
                                    action = openSettingsAction
                                )
                    }
                }

        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState?.getBundle(MAPVIEW_BUNDLE))
        mapView?.getMapAsync {
            map = it
        }

        requestLocationPermissionsLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION))

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        map.isMyLocationEnabled = true
        val locationRequest = LocationRequest.create()
        locationRequest.interval =  5000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d(DEBUG_TAG, "Got location result $locationResult")

                locationResult?.let {
                    for (location: Location? in it.locations) {
                        Log.d(DEBUG_TAG, "Got location in onLocationResult $location")
                        location?.apply {
                            setLocation(this)
                        }
                    }
                }
            }
        }

        locationProviderClient.lastLocation.addOnSuccessListener {
            Log.d(DEBUG_TAG, "Got location in last location $it")

            if (it != null) {
                setLocation(it)
            }
            else {
                locationProviderClient
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }

//        val locationListener = LocationListener {
//            Log.d(DEBUG_TAG, "Got new location $it")
//
//            setLocation(it)
//        }
//        val locationManager = context?.getSystemService(LOCATION_SERVICE) as? LocationManager
//
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        Log.d(DEBUG_TAG, "Requesting location")
//
//        locationManager?.requestLocationUpdates(
//            LocationManager.NETWORK_PROVIDER,
//            0,
//            0f,
//            locationListener
//        )
    }

    private fun setLocation(location: Location) {
        Log.d(DEBUG_TAG, "Setting location...")

        val latLng = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_CAMERA_ZOOM))
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE) ?: Bundle().also {
            outState.putBundle(MAPVIEW_BUNDLE, it)
        }
        mapView?.onSaveInstanceState(mapViewBundle)
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        mapView = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DEFAULT_CAMERA_ZOOM = 15f
    }

    //    private fun showLocationPrompt() {
//        val locationRequest = LocationRequest.create().also {
//            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY }
//        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
//        val result = LocationServices
//            .getSettingsClient(requireActivity())
//            .checkLocationSettings(builder.build())
//
//        result.addOnCompleteListener {
//            try {
//                it.getResult(ApiException::class.java)
//                getLocation()
//            } catch (ex: ApiException) {
//                when (ex.statusCode) {
//                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
//                        try {
//                            val resolvable = ex as ResolvableApiException
//
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            resolvable.startResolutionForResult(
//                                requireActivity(), LocationRequest.PRIORITY_HIGH_ACCURACY)
//                        } catch (ex: Exception) {
//
//                        }
//                    }
//                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
//                    }
//                }
//            }
//        }
//    }
}