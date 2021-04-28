package com.t3ddyss.clother.ui.location_viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentLocationViewerBinding
import com.t3ddyss.clother.utilities.MAPVIEW_BUNDLE

class LocationViewerFragment : Fragment() {

    private var _binding: FragmentLocationViewerBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<LocationViewerFragmentArgs>()

    private var mapView: MapView? = null
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationViewerBinding.inflate(inflater, container, false)
        val (lat, lng) = args.coordinates.split(",").map { it.toDoubleOrNull() }

        if (lat == null || lng == null) {
            (activity as? MainActivity)?.showGenericMessage(getString(R.string.error_showing_location))
            findNavController().popBackStack()
        }
        val point = LatLng(lat!!, lng!!)

        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState?.getBundle(MAPVIEW_BUNDLE))
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            map.uiSettings?.isMyLocationButtonEnabled = false

            val circle = CircleOptions()
                .center(point)
                .radius(RADIUS)
                .fillColor(ContextCompat.getColor(requireContext(), R.color.green_500_map))
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.green_500))
                .strokeWidth(5f)

            map.addCircle(circle)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    point,
                    DEFAULT_CAMERA_ZOOM
                )
            )
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    companion object {
        const val RADIUS = 1_500.0
        const val DEFAULT_CAMERA_ZOOM = 13f
    }
}