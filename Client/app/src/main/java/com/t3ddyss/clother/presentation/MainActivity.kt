package com.t3ddyss.clother.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ActivityMainBinding
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.util.DestinationChangeListener
import com.t3ddyss.core.presentation.NavMenuController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavMenuController {
    private val viewModel by viewModels<MainViewModel>()

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val navController get() = findNavController(R.id.nav_host_fragment)
    private var destinationChangeListener: DestinationChangeListener? = null
    private var destinationChangeListener2: NavController.OnDestinationChangedListener? = null

    override var isMenuVisible
        get() = binding.navView.isVisible
        set(value) {
            binding.navView.isVisible = value
        }
    override val menuView get() = binding.navView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navGraph = navController.navInflater.inflate(R.navigation.main_graph)
        val startDestination = when {
            viewModel.authStateFlow.value is AuthState.Authenticated -> R.id.homeFragment
            viewModel.isOnboardingCompleted -> R.id.signUpFragment
            else -> R.id.onboardingFragment
        }
        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
        binding.navView.setupWithNavController(navController)

        destinationChangeListener = DestinationChangeListener(binding.navView).also {
            navController.addOnDestinationChangedListener(it)
        }
        destinationChangeListener2 = NavController.OnDestinationChangedListener { _, destination, _ ->
            viewModel.onDestinationChange(destination)
        }.also {
            navController.addOnDestinationChangedListener(it)
        }

        viewModel.unauthorizedEvent.observe(this) {
            navController.navigate(R.id.action_global_signUpFragment)
            Handler(Looper.getMainLooper()).post {
                Snackbar.make(
                    binding.root,
                    R.string.error_session_expired,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        destinationChangeListener?.let {
            navController.removeOnDestinationChangedListener(it)
        }
        destinationChangeListener2?.let {
            navController.removeOnDestinationChangedListener(it)
        }
        destinationChangeListener = null
        destinationChangeListener2 = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}