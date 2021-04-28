package com.t3ddyss.clother

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
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
import kotlinx.coroutines.withContext
import java.net.ConnectException
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val messagesViewModel by viewModels<MessagesViewModel>()
    private val networkStateViewModel by viewModels<NetworkStateViewModel>()
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var destinationChangeListener: DestinationChangeListener

    @Inject
    lateinit var prefs: SharedPreferences
    @Inject
    lateinit var onClearFromRecentService: OnClearFromRecentService

    private val openSettingsAction = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    @ExperimentalCoroutinesApi
    private val changeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            run {
                if (key == ACCESS_TOKEN) {
                    messagesViewModel.getMessages(tokenUpdated = true)
                }
            }
        }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        prefs.registerOnSharedPreferenceChangeListener(changeListener)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)

        if (prefs.getBoolean(IS_AUTHENTICATED, false)) {
            navGraph.startDestination = R.id.homeFragment
            messagesViewModel.getMessages()
        } else {
            navGraph.startDestination = R.id.signUpFragment
        }
        navController.graph = navGraph

        // Do not represent actual top-level destinations, just for UP navigation purposes
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.searchFragment, R.id.chatsFragment, R.id.profileFragment,
                R.id.signUpFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        destinationChangeListener = DestinationChangeListener(binding, this).also {
            navController.addOnDestinationChangedListener(it)
        }

        networkStateViewModel.networkAvailability.observe(this) {
            Log.d(DEBUG_TAG, "Network is ${if (it) "connected" else "disconnected"}")

            if (!it) {
                showGenericMessage(getString(R.string.no_connection))
            }
        }

        startService(Intent(applicationContext, OnClearFromRecentService::class.java))

        lifecycleScope.launchWhenCreated {
            setupGoogleMap()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(destinationChangeListener)
        prefs.unregisterOnSharedPreferenceChangeListener(changeListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showGenericMessage(throwable: Throwable) {
        when (throwable) {
            is ConnectException -> showGenericMessage(getString(R.string.no_connection))
            else -> showGenericMessage(null)
        }
    }

    fun showGenericMessage(message: String?) {
        val snackbar = Snackbar.make(
            binding.container,
            message ?: getString(R.string.unknown_error),
            Snackbar.LENGTH_SHORT
        )
        showSnackbarWithMargin(snackbar)
    }

    fun showSnackbarWithAction(
        message: String,
        actionText: String,
        action: (() -> Unit) = openSettingsAction
    ) {
        val snackbar = Snackbar.make(
            binding.navHostFragment,
            message,
            Snackbar.LENGTH_SHORT
        )
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

    fun setNavIconVisibility(isVisible: Boolean) {
        if (!isVisible) {
            binding.toolbar.navigationIcon = null
        } else {
            destinationChangeListener.setIconUp(binding.toolbar)
        }
    }

    private suspend fun setupGoogleMap() = withContext(Dispatchers.Default) {
        try {
            val mapView = MapView(applicationContext)
            mapView.onCreate(null)
            mapView.onPause()
            mapView.onDestroy()
        } catch (ex: Exception) {

        }
    }
}