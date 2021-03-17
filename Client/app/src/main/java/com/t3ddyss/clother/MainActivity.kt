package com.t3ddyss.clother

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.ui.shared_viewmodels.NetworkStateViewModel
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val networkStateViewModel by viewModels<NetworkStateViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    @Inject lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)

        if (prefs.getBoolean(IS_AUTHENTICATED, false) || true) { // for debug purposes
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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            with(binding) {
                when (destination.id) {
                    R.id.signUpFragment -> {
                        toolbar.visibility = View.INVISIBLE
                        navView.visibility = View.GONE
                    }
                    R.id.emailActionFragment, R.id.signInFragment, R.id.resetPasswordFragment -> {
                        toolbar.visibility = View.VISIBLE
                        navView.visibility = View.GONE
                    }
                    else -> {
                        toolbar.visibility = View.VISIBLE
                        navView.visibility = View.VISIBLE
                    }
                }
            }
        }

        setupNetworkStateListener()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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

                Snackbar.make(
                    binding.container,
                    getString(R.string.no_connection),
                    Snackbar.LENGTH_SHORT
                ).show()
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
}