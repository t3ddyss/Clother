package com.t3ddyss.clother.presentation.auth.recovery

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentPasswordRecoveryBinding
import com.t3ddyss.clother.domain.auth.models.ResetPasswordError
import com.t3ddyss.clother.util.extensions.text
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.extensions.textRes
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordRecoveryFragment
    : BaseFragment<FragmentPasswordRecoveryBinding>(FragmentPasswordRecoveryBinding::inflate) {
    private val viewModel by viewModels<PasswordRecoveryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            "",
            ToolbarUtils.NavIcon.UP
        )

        binding.buttonResetPassword.setOnClickListener {
            viewModel.resetPassword(binding.editTextEmail.text())
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.state.collectViewLifecycleAware { state ->
            binding.textInputEmail.isErrorEnabled = false

            when (state) {
                PasswordRecoveryState.Loading -> {
                    binding.layoutLoading.isVisible = true
                }
                is PasswordRecoveryState.ValidationError -> {
                    binding.textInputEmail.error = getString(R.string.auth_email_invalid)
                    binding.textInputEmail.isErrorEnabled = true
                }
                PasswordRecoveryState.Success -> {
                    findNavController()
                        .navigate(
                            PasswordRecoveryFragmentDirections
                                .actionResetPasswordFragmentToEmailActionFragment(
                                    getString(R.string.auth_password_reset_message),
                                    binding.editTextEmail.text()
                                )
                        )
                    binding.layoutLoading.isVisible = false
                }
                PasswordRecoveryState.Error -> {
                    binding.layoutLoading.isVisible = false
                }
            }
        }

        viewModel.error.collectViewLifecycleAware { event ->
            event.getContentOrNull()?.let { showSnackbarWithText(it.textRes) }
        }
    }

    private val ResetPasswordError.textRes get() = when (this) {
        ResetPasswordError.UserNotFound -> R.string.auth_user_not_found
        is ResetPasswordError.Common -> error.textRes
    }
}