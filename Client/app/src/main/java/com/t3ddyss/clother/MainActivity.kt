package com.t3ddyss.clother

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Gravity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.transition.AutoTransition
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.viewmodels.NetworkStateViewModel
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import dagger.hilt.android.AndroidEntryPoint
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val networkStateViewModel by viewModels<NetworkStateViewModel>()
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private var destinationChangeListener: DestinationChangeListener? = null

    @Inject lateinit var prefs: SharedPreferences

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

        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.homeFragment, R.id.messagesFragment, R.id.profileFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        destinationChangeListener = DestinationChangeListener(binding).also {
            navController.addOnDestinationChangedListener(it)
        }

        setupNetworkStateListener()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showGenericError(throwable: Throwable) {
        when (throwable) {
            is SocketTimeoutException -> showGenericError(null)
            !is ConnectException -> showConnectionError()
        }
    }

    fun showGenericError(message: String?) {
        Snackbar.make(binding.container,
                message ?:
                getString(R.string.unknown_error),
                Snackbar.LENGTH_SHORT).show()
    }

    fun showConnectionError() {
        Snackbar.make(binding.container,
                getString(R.string.no_connection),
                Snackbar.LENGTH_SHORT).show()
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

                showConnectionError()
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

    class DestinationChangeListener(
        private val binding: ActivityMainBinding)
    : NavController.OnDestinationChangedListener {
        private val fragmentsWithoutToolbar = setOf(R.id.signUpFragment,
                R.id.offerEditorFragment)

        private val fragmentsWithoutBottomNav = setOf(R.id.emailActionFragment,
        R.id.offerEditorFragment, R.id.resetPasswordFragment, R.id.signInFragment,
        R.id.signUpFragment)

        private val fragmentsWithInvisibleToolbar = setOf(R.id.signUpFragment)

        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            with (binding) {
                if (destination.id !in fragmentsWithoutBottomNav) {
                    if (!navView.isVisible) {
                        animateBottomNav()
                        navView.isVisible = true
                    }
                }
                else {
                    if (navView.isVisible) {
                        animateBottomNav()
                        navView.isVisible = false
                    }
                }

                if (destination.id !in fragmentsWithoutToolbar) {
                    if (!toolbar.isVisible) {
                        animateToolbar()
                        toolbar.isVisible = true
                    }
                }
                else {
                    if (toolbar.isVisible) {
                        if (destination.id !in fragmentsWithInvisibleToolbar){
                            animateToolbar()
                            toolbar.isVisible = false
                        }
                        else {
                            toolbar.isInvisible = true
                        }
                    }
                }
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
    }
}