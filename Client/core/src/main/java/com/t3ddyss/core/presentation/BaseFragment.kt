package com.t3ddyss.core.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.core.R
import java.net.ConnectException

abstract class BaseFragment<B: ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> B
) : Fragment() {
    private var _binding: B? = null
    protected val binding get() = _binding!!
    private var navMenuController: NavMenuController? = null

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        navMenuController = activity as? NavMenuController
        return binding.root
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun showErrorMessage(throwable: Throwable) {
        val message = when (throwable) {
            is ConnectException -> getString(R.string.no_connection)
            else -> null
        }
        showGenericMessage(message)
    }

    protected fun showGenericMessage(message: String?) {
        val snackbar = Snackbar.make(
            requireView(),
            message ?: getString(R.string.unknown_error),
            Snackbar.LENGTH_SHORT
        )
        showSnackBar(snackbar)
    }

    protected fun showMessageWithAction(
        message: String,
        actionText: String,
        action: (() -> Unit)
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
        if (navMenuController?.isMenuVisible == true) {
            snackbar.anchorView = navMenuController?.menuView
        }
        snackbar.show()
    }
}