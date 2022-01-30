package com.t3ddyss.feature_location.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.feature_location.R

abstract class BaseLocationFragment<B: ViewBinding>(
    inflate: (LayoutInflater, ViewGroup?, Boolean) -> B
) : BaseFragment<B>(inflate) {

    protected var map: GoogleMap? = null
    protected var mapView: MapView? = null

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapView = binding.root.findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    @CallSuper
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE) ?: Bundle().also {
            outState.putBundle(MAPVIEW_BUNDLE, it)
        }
        mapView?.onSaveInstanceState(mapViewBundle)
    }

    @CallSuper
    override fun onDestroyView() {
        mapView?.onDestroy()
        mapView = null
        map = null
        super.onDestroyView()
    }

    companion object {
        private const val MAPVIEW_BUNDLE = "MAPVIEW_BUNDLE"
    }
}