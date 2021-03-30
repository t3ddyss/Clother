package com.t3ddyss.clother.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentLocationBinding
import com.t3ddyss.clother.ui.offer_editor.OfferEditorViewModel

@SuppressLint("MissingPermission")
class LocationFragment : Fragment() {

    private val viewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private var mapView: MapView? = null
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)

        val openSettingsAction = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        val requestLocationPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        map.isMyLocationEnabled = true
                    } else {
                        (activity as? MainActivity)
                                ?.showSnackbarWithAction(
                                        message = getString(R.string.no_gallery_access),
                                        actionText = getString(R.string.grant_access),
                                        action = openSettingsAction
                                )
                    }
                }

        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync {
            map = it
            it.uiSettings.isMyLocationButtonEnabled = false
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        return binding.root
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
        mapView?.onSaveInstanceState(outState)
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
}