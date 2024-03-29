package com.t3ddyss.feature_location.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
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
import com.t3ddyss.core.util.extensions.showSnackbarWithAction
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.IntentUtils
import com.t3ddyss.core.util.utils.ToolbarUtils
import com.t3ddyss.feature_location.R
import com.t3ddyss.feature_location.databinding.FragmentLocationSelectorBinding
import com.t3ddyss.feature_location.domain.models.LocationType
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
            requestLocation()
        } else {
            setOnMapLongClickListener()
            showSnackbarWithText(getString(R.string.location_no_access))
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.location_select),
            ToolbarUtils.NavIcon.CLOSE
        )

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
                showSnackbarWithAction(
                        text = R.string.location_no_access,
                        actionText = R.string.action_grant_permission,
                        action = { IntentUtils.openApplicationSettings(requireContext())}
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
        return when (item.itemId) {
            R.id.apply -> {
                onApplyClick()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun subscribeUi() {
        viewModel.location.observe(viewLifecycleOwner) {
            when (it.locationType) {
                LocationType.INITIAL -> setInitialLocation(it.latLng)
                LocationType.MANUALLY_SELECTED -> setLocationWithMarker(it.latLng)
                LocationType.DETECTED_BY_GPS -> moveCameraToLocation(it.latLng)
            }
        }
    }

    private fun requestLocation() {
        viewModel.requestLocation()
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

    private fun moveCameraToLocation(latLng: LatLng) {
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                DEFAULT_CAMERA_ZOOM
            )
        )
    }

    private fun setOnMapLongClickListener() {
        map?.setOnMapLongClickListener(viewModel::setLocationManually)
    }

    private fun onApplyClick() {
        viewModel.saveLocation()
        viewModel.location.value?.let {
            if (it.locationType != LocationType.INITIAL) {
                setFragmentResult(
                    COORDINATES_KEY,
                    bundleOf(COORDINATES to it.latLng)
                )
                findNavController().popBackStack()
            }
        }
    }

    private fun checkIfLocationEnabled() {
        if (viewModel.isLocationEnablingRequested.getAndSet(true)) return

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val result = LocationServices
            .getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                requestLocation()
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

    companion object {
        const val COORDINATES_KEY = "coordinates_key"
        const val COORDINATES = "coordinates"
        private const val DEFAULT_CAMERA_ZOOM = 15f
        private const val INITIAL_CAMERA_ZOOM = 3.5f
    }
}