package com.t3ddyss.clother.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.NavMenuState
import com.t3ddyss.clother.R
import java.net.ConnectException

abstract class BaseFragment<B: ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> B
) : Fragment() {
    private var _binding: B? = null
    protected val binding get() = _binding!!
    private lateinit var navMenuState: NavMenuState

    private val openSettingsAction = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        navMenuState = activity as NavMenuState
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showErrorMessage(throwable: Throwable) {
        val message = when (throwable) {
            is ConnectException -> getString(R.string.no_connection)
            else -> null
        }
        showGenericMessage(message)
    }

    fun showGenericMessage(message: String?) {
        val snackbar = Snackbar.make(
            requireView(),
            message ?: getString(R.string.unknown_error),
            Snackbar.LENGTH_SHORT
        )
        showSnackBar(snackbar)
    }

    fun showMessageWithAction(
        message: String,
        actionText: String,
        action: (() -> Unit) = openSettingsAction
    ) {
        val snackbar = Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction(actionText) {
                action.invoke()
        }
        showSnackBar(snackbar)
    }

    private fun showSnackBar(snackbar: Snackbar) {
        if (navMenuState.isNavMenuVisible) {
            snackbar.setAnchorView(R.id.nav_view)
        }
        snackbar.show()
    }
}