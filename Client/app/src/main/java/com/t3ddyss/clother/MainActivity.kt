package com.t3ddyss.clother

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.models.domain.AuthState
import com.t3ddyss.clother.utilities.DestinationChangeListener
import com.t3ddyss.clother.utilities.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavMenuState {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var destinationChangeListener: DestinationChangeListener
    override val isNavMenuVisible get() = binding.navView.isVisible

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)

        if (viewModel.authStateFlow.value is AuthState.Authenticated) {
            navGraph.startDestination = R.id.homeFragment
        } else {
            navGraph.startDestination = R.id.signUpFragment
        }
        navController.graph = navGraph

        // Do not represent actual top-level destinations, just for UP navigation purposes
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.searchFragment,
                R.id.chatsFragment, R.id.profileFragment,
                R.id.signUpFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        destinationChangeListener =
            DestinationChangeListener(binding, this, notificationHelper).also {
                navController.addOnDestinationChangedListener(it)
            }

        viewModel.unauthorizedEvent.observe(this) {
            if (navController.currentDestination?.id != R.id.signUpFragment) {
                navController.navigate(R.id.action_global_signUpFragment)
                Handler(Looper.getMainLooper()).post {
                    Snackbar.make(
                        binding.root,
                        R.string.session_expired,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(destinationChangeListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}

interface NavMenuState {
    val isNavMenuVisible: Boolean
}