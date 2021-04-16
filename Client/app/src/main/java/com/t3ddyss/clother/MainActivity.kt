package com.t3ddyss.clother

import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
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
import com.google.android.gms.maps.MapView
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.services.OnClearFromRecentService
import com.t3ddyss.clother.utilities.*
import com.t3ddyss.clother.viewmodels.MessagesViewModel
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

    private val messagesViewModel by viewModels<MessagesViewModel>()
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
                R.id.homeFragment, R.id.searchFragment, R.id.chatsFragment, R.id.profileFragment,
        R.id.signUpFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        destinationChangeListener = DestinationChangeListener(binding).also {
            navController.addOnDestinationChangedListener(it)
        }

        setupGoogleMap()
        setupNetworkStateListener()

        startService(Intent(applicationContext, OnClearFromRecentService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(destinationChangeListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showGenericMessage(throwable: Throwable) {
        when (throwable) {
            is SocketTimeoutException -> showGenericMessage(null)
            is ConnectException -> showGenericMessage(getString(R.string.no_connection))
        }
    }

    fun showGenericMessage(message: String?) {
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

    private fun setupGoogleMap() {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val mapView = MapView(applicationContext)
                mapView.onCreate(null)
                mapView.onPause()
                mapView.onDestroy()
            } catch (ex: Exception) {

            }
        }
    }

    // TODO replace Pair with data class
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

                showGenericMessage(getString(R.string.no_connection))
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
            R.id.offerFragment, R.id.locationViewerFragment, R.id.searchFragment, R.id.chatFragment)

        private val fragmentsWithoutToolbar = setOf(R.id.searchFragment)

        private val fragmentsWithToolbarLabel = setOf(R.id.offerCategoryFragment,
                R.id.offerEditorFragment, R.id.galleryFragment, R.id.locationFragment,
            R.id.locationViewerFragment, R.id.searchByCategoryFragment, R.id.chatFragment,
        R.id.homeFragment, R.id.chatsFragment)

        private val fragmentsWithoutNavIcon = setOf(R.id.homeFragment,
                R.id.profileFragment, R.id.searchByCategoryFragment,
        R.id.searchFragment, R.id.signUpFragment, R.id.chatsFragment)

        private val fragmentsWithCustomUpIcon = setOf(R.id.offerEditorFragment,
                R.id.galleryFragment, R.id.locationFragment)

        private val fragmentsOverlayingToolbar = setOf(R.id.offerFragment)

        override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
        ) {
            messagesViewModel.setIsChatsDestination(destination.id == R.id.chatsFragment,
            destination.id == R.id.chatFragment)

            with(binding) {
                // NavView visibility
                if (destination.id !in fragmentsWithoutBottomNav && !navView.isVisible) {
                    navView.isVisible = true
                }
                else if (destination.id in fragmentsWithoutBottomNav && navView.isVisible) {
                    navView.isVisible = false
                }

                // Toolbar visibility
                if (destination.id !in fragmentsWithoutToolbar && !toolbar.isVisible) {
                    toolbar.isVisible = true
                }
                else if (destination.id in fragmentsWithoutToolbar && toolbar.isVisible) {
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