package com.t3ddyss.clother.presentation.auth.signin

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentSignInBinding
import com.t3ddyss.clother.domain.auth.models.SignInError
import com.t3ddyss.clother.util.extensions.text
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.extensions.textRes
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment
    : BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {
    private val viewModel by viewModels<SignInViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            "",
            ToolbarUtils.NavIcon.UP
        )

        binding.buttonSignIn.setOnClickListener {
            viewModel.signIn(
                email = binding.editTextEmail.text(),
                password = binding.editTextPassword.text()
            )
        }

        binding.textViewResetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_resetPasswordFragment)
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.state.collectViewLifecycleAware { state ->
            binding.textInputEmail.isErrorEnabled = false

            when (state) {
                SignInState.Loading -> {
                    binding.layoutLoading.isVisible = true
                }

                is SignInState.ValidationError -> {
                    binding.textInputEmail.error = getString(R.string.auth_email_invalid)
                    binding.textInputEmail.isErrorEnabled = true
                }

                SignInState.Success -> {
                    findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                }

                SignInState.Error -> {
                    binding.layoutLoading.isVisible = false
                }
            }
        }

        viewModel.error.collectViewLifecycleAware { event ->
            event.getContentOrNull()?.let { showSnackbarWithText(it.textRes) }
        }
    }

    private val SignInError.textRes get() = when (this) {
        SignInError.InvalidCredentials -> R.string.auth_invalid_credentials
        SignInError.EmailNotVerified -> R.string.auth_email_not_verified
        is SignInError.Common -> error.textRes
    }
}