package com.t3ddyss.clother.ui.location_selector

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentLocationSelectorBinding
import com.t3ddyss.clother.ui.filters.FiltersViewModel
import com.t3ddyss.clother.ui.offer_editor.OfferEditorViewModel
import com.t3ddyss.clother.utilities.MAPVIEW_BUNDLE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class LocationSelectorFragment : Fragment() {

    private val viewModel by viewModels<LocationSelectorViewModel>()
    private val editorViewModel by hiltNavGraphViewModels<OfferEditorViewModel>(
        R.id.offer_editor_graph
    )
    private val filtersViewModel by hiltNavGraphViewModels<FiltersViewModel>(
        R.id.search_results_graph
    )

    private var _binding: FragmentLocationSelectorBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<LocationSelectorFragmentArgs>()

    private var mapView: MapView? = null
    private lateinit var map: GoogleMap
    private var isPermissionGranted = false

    @ExperimentalCoroutinesApi
    private val enableLocationDialogLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getLocation()
        } else {
            setOnMapLongClickListener()
            (activity as? MainActivity)?.showGenericMessage(
                message = getString(R.string.no_location_access)
            )
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationSelectorBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if (isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true
                && isGranted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                isPermissionGranted = true

                if (this::map.isInitialized) {
                    map.isMyLocationEnabled = true
                }

                checkIfLocationEnabled()
            } else {
                (activity as? MainActivity)
                    ?.showSnackbarWithAction(
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

        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState?.getBundle(MAPVIEW_BUNDLE))
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            map.uiSettings.isMyLocationButtonEnabled = false

            if (isPermissionGranted) {
                map.isMyLocationEnabled = true
            }

            subscribeToLocationUpdates()
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
                when (args.calledFromId) {
                    R.id.offer_editor_graph -> editorViewModel.location.value = location.latLng
                    R.id.search_results_graph -> filtersViewModel.location.value = location.latLng
                    else -> {
                    }
                }

                findNavController().popBackStack()

                viewModel.saveSelectedLocation(location)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun subscribeToLocationUpdates() {
        viewModel.location.observe(viewLifecycleOwner) {
            when {
                it.isInitialValue -> {
                    setInitialLocation(it.latLng)
                }
                it.isManuallySelected -> {
                    setLocationWithMarker(it.latLng)
                }
                else -> {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            it.latLng,
                            DEFAULT_CAMERA_ZOOM
                        )
                    )
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun getLocation() {
        viewModel.getLocation()
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
                .visible(true)
        )
    }

    private fun setOnMapLongClickListener() {
        map.setOnMapLongClickListener {
            viewModel.setLocationManually(it)
        }
    }

    @ExperimentalCoroutinesApi
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
                                enableLocationDialogLauncher.launch(
                                    IntentSenderRequest.Builder(exception.resolution).build()
                                )
                            }
                        } catch (ex: Exception) {
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DEFAULT_CAMERA_ZOOM = 15f
        const val INITIAL_CAMERA_ZOOM = 3.5f
    }
}