package com.t3ddyss.feature_location.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.t3ddyss.feature_location.R
import com.t3ddyss.feature_location.databinding.FragmentLocationSelectorBinding
import com.t3ddyss.navigation.util.setNavigationResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationSelectorFragment
    : BaseLocationFragment<FragmentLocationSelectorBinding>(FragmentLocationSelectorBinding::inflate) {

    private val viewModel by viewModels<LocationSelectorViewModel>()

    private var isPermissionGranted = false

    private val enableLocationDialogLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getLocation()
        } else {
            setOnMapLongClickListener()
            showGenericMessage(getString(R.string.no_location_access))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                isGranted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                isPermissionGranted = true

                map?.apply {
                    isMyLocationEnabled = true
                }
                checkIfLocationEnabled()
            } else {
                showMessageWithAction(
                        message = getString(R.string.no_location_access),
                        actionText = getString(R.string.grant_permission)
                )
                setOnMapLongClickListener()
            }
        }.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        mapView?.getMapAsync { googleMap ->
            map = googleMap
            map?.let {
                it.uiSettings.isMyLocationButtonEnabled = false
                it.isMyLocationEnabled = isPermissionGranted
            }
            subscribeUi()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_apply_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.apply) {
            viewModel.location.value?.let {
                if (!it.isInitialValue) {
                    val latLng = it.latLng
                    viewModel.saveSelectedLocation(it)
                    setNavigationResult(COORDINATES_KEY, "${latLng.latitude},${latLng.longitude}")
                    findNavController().popBackStack()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeUi() {
        viewModel.location.observe(viewLifecycleOwner) {
            when {
                it.isInitialValue -> setInitialLocation(it.latLng)
                it.isManuallySelected -> setLocationWithMarker(it.latLng)
                else -> {
                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            it.latLng,
                            DEFAULT_CAMERA_ZOOM
                        )
                    )
                }
            }
        }
    }

    private fun getLocation() {
        viewModel.getLocation()
    }

    private fun setInitialLocation(latLng: LatLng) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_CAMERA_ZOOM))
    }

    private fun setLocationWithMarker(latLng: LatLng) {
        map?.clear()
        map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .draggable(false)
                .visible(true)
        )
    }

    private fun setOnMapLongClickListener() {
        map?.setOnMapLongClickListener {
            viewModel.setLocationManually(it)
        }
    }

    private fun checkIfLocationEnabled() {
        if (viewModel.isEnablingLocationRequested) return
        viewModel.isEnablingLocationRequested = true

        val locationRequest = LocationRequest.create().also {
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val result = LocationServices
            .getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                getLocation()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            if (exception is ResolvableApiException) {
                                enableLocationDialogLauncher
                                    .launch(
                                        IntentSenderRequest.Builder(exception.resolution).build()
                                    )
                            }
                        } catch (ex: Exception) {
                        }
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

    companion object {
        const val COORDINATES_KEY = "coordinates_key"
        private const val DEFAULT_CAMERA_ZOOM = 15f
        private const val INITIAL_CAMERA_ZOOM = 3.5f
    }
}