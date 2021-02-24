package com.t3ddyss.clother

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.utilities.AUTHENTICATED
import com.t3ddyss.clother.utilities.SAD_FACE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val sp by lazy { getPreferences(Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (sp.getBoolean(AUTHENTICATED, false)) {
            val navGraph = navController.graph
            navGraph.startDestination = R.id.homeFragment
            navController.graph = navGraph
        }

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

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {

            }

            override fun onLost(network: Network) {
                Snackbar.make(binding.container,
                        getString(R.string.no_connection) + SAD_FACE,
                        Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}