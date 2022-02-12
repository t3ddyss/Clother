package com.t3ddyss.feature_location.presentation

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.feature_location.R
import com.t3ddyss.feature_location.databinding.FragmentLocationViewerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationViewerFragment
    : BaseLocationFragment<FragmentLocationViewerBinding>(FragmentLocationViewerBinding::inflate) {
    private val args by navArgs<LocationViewerFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val (lat, lng) = args.coordinates
            .split(",")
            .map { it.toDoubleOrNull() }

        if (lat == null || lng == null) {
            showGenericMessage(getString(R.string.error_showing_location))
            findNavController().popBackStack()
        }
        val point = LatLng(lat!!, lng!!)
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            googleMap.uiSettings.isMyLocationButtonEnabled = false

            val circle = CircleOptions()
                .center(point)
                .radius(RADIUS)
                .fillColor(ContextCompat.getColor(requireContext(), R.color.green_500_map))
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.green_500))
                .strokeWidth(5f)

            googleMap.addCircle(circle)
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    point,
                    DEFAULT_CAMERA_ZOOM
                )
            )
        }
    }

    companion object {
        private const val RADIUS = 1_500.0
        private const val DEFAULT_CAMERA_ZOOM = 13f
    }
}