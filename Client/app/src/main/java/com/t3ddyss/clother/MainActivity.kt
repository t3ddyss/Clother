package com.t3ddyss.clother

import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.transition.AutoTransition
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.gms.maps.MapView
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.services.OnClearFromRecentService
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import com.t3ddyss.clother.utilities.convertDpToPx
import com.t3ddyss.clother.utilities.getThemeColor
import com.t3ddyss.clother.utilities.toColorFilter
import com.t3ddyss.clother.viewmodels.NetworkStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val networkStateViewModel by viewModels<NetworkStateViewModel>()
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var destinationChangeListener: DestinationChangeListener

    @Inject lateinit var prefs: SharedPreferences
    @Inject lateinit var onClearFromRecentService: OnClearFromRecentService

    private val openSettingsAction = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)

        if (prefs.getBoolean(IS_AUTHENTICATED, false)) {
            navGraph.startDestination = R.id.homeFragment
        }
        else {
            navGraph.startDestination = R.id.signUpFragment
        }
        navController.graph = navGraph

        // Do not represent actual top-level destinations, just for UP navigation purposes
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.homeFragment, R.id.messagesFragment, R.id.profileFragment, R.id.searchFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        destinationChangeListener = DestinationChangeListener(binding).also {
            navController.addOnDestinationChangedListener(it)
        }

        setupNetworkStateListener()

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val mapView = MapView(applicationContext)
                mapView.onCreate(null)
                mapView.onPause()
                mapView.onDestroy()
            } catch (ex: Exception) {

            }
        }

        startService(Intent(applicationContext, OnClearFromRecentService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(destinationChangeListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showGenericDialog(message: String?) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirmation))
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
    }

    fun showGenericError(throwable: Throwable) {
        when (throwable) {
            is SocketTimeoutException -> showGenericError(null)
            is ConnectException -> showGenericError(getString(R.string.no_connection)) // Fix
        }
    }

    fun showGenericError(message: String?) {
        val snackbar = Snackbar.make(binding.container,
                message ?: getString(R.string.unknown_error),
                Snackbar.LENGTH_SHORT)
        showSnackbarWithMargin(snackbar)
    }

    fun showSnackbarWithAction(message: String,
                               actionText: String,
                               action: (() -> Unit) = openSettingsAction) {
        val snackbar = Snackbar.make(binding.navHostFragment,
                message,
                Snackbar.LENGTH_SHORT)
                .setAction(actionText) {
                    action.invoke()
                }
        showSnackbarWithMargin(snackbar)
    }

    private fun showSnackbarWithMargin(snackbar: Snackbar) {
        val margin = if (binding.navView.isVisible) 60 else 8
        val snackBarView = snackbar.view
        snackBarView.translationY = (-1) * this.convertDpToPx(margin).toFloat()
        snackbar.show()
    }

    private fun setupNetworkStateListener() {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE)
                as? ConnectivityManager ?: return

        val isNetworkAvailable = isNetworkAvailable(connectivityManager)
        if (isNetworkAvailable) {
            networkStateViewModel.isNetworkAvailable.value = Pair(
                    first = false,
                    second = true
            )
        }
        else {
            networkStateViewModel.isNetworkAvailable.value = Pair(
                    first = true,
                    second = false
            )
        }

        connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasNetworkAvailable = networkStateViewModel.isNetworkAvailable.value!!.second

                if (!wasNetworkAvailable) {
                    networkStateViewModel.isNetworkAvailable.postValue(Pair(
                            first = true,
                            second = true
                    ))
                }
            }

            override fun onLost(network: Network) {
                networkStateViewModel.isNetworkAvailable.postValue(Pair(
                        first = true,
                        second = false
                ))

                showGenericError(getString(R.string.no_connection))
            }
        })
    }

    private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return with(networkCapabilities) {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
    }

    private fun animateBottomNav(gravity: Int = Gravity.BOTTOM) {
        TransitionManager
                .beginDelayedTransition(
                        binding.navView,
                        Slide(gravity),
//                        AutoTransition()
                )
    }

    private fun animateToolbar(gravity: Int = Gravity.TOP) {
        TransitionManager
                .beginDelayedTransition(
                        binding.toolbar,
//                            Slide(gravity),
                        AutoTransition()
                )
    }

    fun setNavIconVisibility(isVisible: Boolean) {
        if (!isVisible) {
            binding.toolbar.navigationIcon = null
        }
        else {
            destinationChangeListener.setIconUp(binding.toolbar)
        }
    }

    fun setLoadingVisibility(isVisible: Boolean) {
        binding.layoutLoading.isVisible = isVisible
    }

    inner class DestinationChangeListener(
            private val binding: ActivityMainBinding)
    : NavController.OnDestinationChangedListener {
        private val fragmentsWithoutBottomNav = setOf(R.id.emailActionFragment,
                R.id.offerEditorFragment, R.id.resetPasswordFragment, R.id.signInFragment,
                R.id.signUpFragment, R.id.galleryFragment, R.id.locationFragment,
            R.id.offerFragment, R.id.locationViewerFragment, R.id.searchFragment)

        private val fragmentsWithoutToolbar = setOf(R.id.searchFragment)

        private val fragmentsWithToolbarLabel = setOf(R.id.offerCategoryFragment,
                R.id.offerEditorFragment, R.id.galleryFragment, R.id.locationFragment,
            R.id.locationViewerFragment, R.id.searchByCategoryFragment)

        private val fragmentsWithoutNavIcon = setOf(R.id.homeFragment,
                R.id.messagesFragment, R.id.profileFragment, R.id.searchByCategoryFragment,
        R.id.searchFragment, R.id.signUpFragment)

        private val fragmentsWithCustomUpIcon = setOf(R.id.offerEditorFragment,
                R.id.galleryFragment, R.id.locationFragment)

        private val fragmentsOverlayingToolbar = setOf(R.id.offerFragment)

        override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
        ) {
            with(binding) {
                // NavView visibility
                if (destination.id !in fragmentsWithoutBottomNav && !navView.isVisible) {
//                    animateBottomNav()
                    navView.isVisible = true
                }
                else if (destination.id in fragmentsWithoutBottomNav && navView.isVisible) {
//                    animateBottomNav()
                    navView.isVisible = false
                }

                // Toolbar visibility
                if (destination.id !in fragmentsWithoutToolbar && !toolbar.isVisible) {
//                    animateToolbar()
                    toolbar.isVisible = true
                }
                else if (destination.id in fragmentsWithoutToolbar && toolbar.isVisible) {
//                    animateToolbar()
                    toolbar.isVisible = false
                }
                binding.navHostFragmentMarginTop.isVisible =
                        destination.id !in fragmentsOverlayingToolbar
                                && destination.id !in fragmentsWithoutToolbar

                // Toolbar icon
                if (destination.id !in fragmentsWithoutNavIcon
                        && destination.id in fragmentsWithCustomUpIcon) {
                    setIconClose(toolbar)
                }
                else if (destination.id !in fragmentsWithoutNavIcon){
                    setIconUp(toolbar)
                }

                // Toolbar title
                if (destination.id in fragmentsWithToolbarLabel) {
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
                else {
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                }
            }
        }

        private fun setIconClose(toolbar: Toolbar) {
            toolbar.setNavigationIcon(R.drawable.ic_close)
            toolbar.navigationIcon?.colorFilter = getThemeColor(R.attr.colorOnPrimary).toColorFilter()
        }

        fun setIconUp(toolbar: Toolbar) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            toolbar.navigationIcon?.colorFilter = getThemeColor(R.attr.colorOnPrimary).toColorFilter()
        }
    }
}