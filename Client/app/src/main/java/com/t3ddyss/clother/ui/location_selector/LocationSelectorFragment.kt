package com.t3ddyss.clother.ui.location_selector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentLocationSelectorBinding
import com.t3ddyss.clother.models.LatLngWrapper
import com.t3ddyss.clother.ui.offer_editor.OfferEditorViewModel
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.MAPVIEW_BUNDLE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi// TODO encapsulate location detection logic in a separate class
class LocationSelectorFragment : Fragment() {

    private val viewModel by viewModels<LocationSelectorViewModel>()
    private val editorViewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)

    private var _binding: FragmentLocationSelectorBinding? = null
    private val binding get() = _binding!!

    private var mapView: MapView? = null
    private lateinit var map: GoogleMap
    private lateinit var locationProviderClient: FusedLocationProviderClient

    private val locationCallback = LocationListener()
    private val enableLocationDialogLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getLocation()
        }
        else {
            (activity as? MainActivity)?.showGenericError(
                    message = getString(R.string.no_location_access)
            )
            map.setOnMapLongClickListener {
                viewModel.location.value = LatLngWrapper(
                        latLng = it,
                        isInitialValue = false,
                        isManuallySelected = true,
                )
            }
        }}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationSelectorBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    && isGranted[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                checkIfLocationEnabled()
            }
            else {
                (activity as? MainActivity)
                        ?.showSnackbarWithAction(
                            message = getString(R.string.no_location_access),
                            actionText = getString(R.string.grant_permission)
                        )
                map.setOnMapLongClickListener {
                    viewModel.location.value = LatLngWrapper(
                            latLng = it,
                            isInitialValue = false,
                            isManuallySelected = true,
                    )
                }
            }
        }.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION))

        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState?.getBundle(MAPVIEW_BUNDLE))
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            map.uiSettings?.isMyLocationButtonEnabled = false

            viewModel.location.observe(viewLifecycleOwner) {
                when {
                    it.isInitialValue -> {
                        setInitialLocation(it.latLng)
                    }
                    it.isManuallySelected -> {
                        setLocationWithMarker(it.latLng)
                    }
                    else -> {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(it.latLng, DEFAULT_CAMERA_ZOOM))
                    }
                }
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_apply_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.apply) {
            val location = viewModel.location.value!!
            if (!location.isInitialValue) {
                editorViewModel.location.value = location.latLng
                findNavController().popBackStack()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        map.isMyLocationEnabled = true

        val locationRequest = LocationRequest.create()
        locationRequest.interval =  5_000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationProviderClient.lastLocation.addOnSuccessListener {
            Log.d(DEBUG_TAG, "Got location in last location $it")

            if (it != null) {
                viewModel.location.postValue(LatLngWrapper(
                        latLng = LatLng(it.latitude, it.longitude),
                        isInitialValue = false,
                        isManuallySelected = false
                ))
            }
            else {
                Toast.makeText(context, getString(R.string.updating_location), Toast.LENGTH_LONG).show()
                locationProviderClient
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }
    }

    private fun setInitialLocation(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_CAMERA_ZOOM))
    }

    private fun setLocationWithMarker(latLng: LatLng) {
        map.clear()
        map.addMarker(
                MarkerOptions()
                        .position(latLng)
                        .draggable(false)
                        .visible(true))
    }

    private fun checkIfLocationEnabled() {
        if (viewModel.isEnablingLocationRequested) return
        viewModel.isEnablingLocationRequested = true

        val locationRequest = LocationRequest.create().also {
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val result = LocationServices
                .getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                getLocation()
            }
            catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            if (exception is ResolvableApiException) {
                                enableLocationDialogLauncher.launch(
                                        IntentSenderRequest.Builder(exception.resolution).build())
                            }
                        } catch (ex: Exception) { }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { }
                }
            }
        }
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
        locationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DEFAULT_CAMERA_ZOOM = 15f
        const val INITIAL_CAMERA_ZOOM = 3.5f
    }

    inner class LocationListener : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                for (location: Location? in it.locations) {
                    Log.d(DEBUG_TAG, "Got location in onLocationResult $location")

                    location?.apply {
                        viewModel.location.postValue(LatLngWrapper(
                                latLng = LatLng(this.latitude, this.longitude),
                                isInitialValue = false,
                                isManuallySelected = false,
                        ))
                    }
                }
            }
        }
    }
}