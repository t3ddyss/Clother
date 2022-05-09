package com.t3ddyss.clother.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentPasswordRecoveryBinding
import com.t3ddyss.clother.domain.common.common.models.Response
import com.t3ddyss.clother.util.text
import com.t3ddyss.clother.util.toEditable
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.StringUtils
import com.t3ddyss.core.util.ToolbarUtils
import com.t3ddyss.core.util.showSnackbarWithText
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
            val email = binding.editTextEmail.text()

            if (!StringUtils.isValidEmail(email)) {
                binding.textInputEmail.error = getString(R.string.auth_email_invalid)
                return@setOnClickListener
            }
            binding.textInputEmail.isErrorEnabled = false

            viewModel.resetPassword(email)
        }

        subscribeUi()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveEmail(binding.editTextEmail.text())
    }

    private fun subscribeUi() {
        viewModel.email.observe(viewLifecycleOwner) {
            binding.editTextEmail.text = it.toEditable()
        }

        viewModel.passwordRecoveryResult.observe(viewLifecycleOwner) {
            if (it.hasBeenHandled && it.peekContent() is Success<*>) return@observe

            when (val response = it.getContentIfNotHandled() ?: return@observe) {
                is Loading<*> ->
                    binding.layoutLoading.isVisible = true
                is Success<Response> -> {
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
                is Error<Response> -> {
                    binding.layoutLoading.isVisible = false
                    showSnackbarWithText(response.message)
                }
            }
        }
    }
}