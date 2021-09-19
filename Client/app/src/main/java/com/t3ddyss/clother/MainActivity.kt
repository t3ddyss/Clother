package com.t3ddyss.clother

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.DestinationChangeListener
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import com.t3ddyss.clother.utilities.NotificationUtil
import com.t3ddyss.clother.viewmodels.MessagesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val messagesViewModel by viewModels<MessagesViewModel>()
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var destinationChangeListener: DestinationChangeListener

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var notificationUtil: NotificationUtil

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

        destinationChangeListener =
            DestinationChangeListener(binding, this, notificationUtil).also {
                navController.addOnDestinationChangedListener(it)
            }

//        networkStateViewModel.networkAvailability.observe(this) {
//            Log.d(DEBUG_TAG, "Network is ${if (it) "connected" else "disconnected"}")
//
//            if (!it) {
//                showGenericMessage(getString(R.string.no_connection))
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(destinationChangeListener)
        prefs.unregisterOnSharedPreferenceChangeListener(changeListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}