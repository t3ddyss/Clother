package com.t3ddyss.clother.presentation.auth.signup

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentSignUpBinding
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.SignUpError
import com.t3ddyss.clother.util.extensions.text
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.extensions.textRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment
    : BaseFragment<FragmentSignUpBinding>(FragmentSignUpBinding::inflate) {
    private val viewModel by viewModels<SignUpViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonSignUp.setOnClickListener {
            viewModel.createUserWithCredentials(
                name = binding.editTextName.text(),
                email = binding.editTextEmail.text(),
                password = binding.editTextPassword.text()
            )
        }

        binding.textViewSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.state.collectViewLifecycleAware { state ->
            clearValidationErrors()

            when (state) {
                SignUpState.Loading -> {
                    binding.layoutLoading.isVisible = true
                }

                is SignUpState.ValidationError -> {
                    setValidationErrors(state)
                }

                SignUpState.Success -> {
                    findNavController().navigate(
                        SignUpFragmentDirections.actionSignUpFragmentToEmailActionFragment(
                            getString(R.string.auth_email_activation_message),
                            binding.editTextEmail.text()
                        )
                    )
                }

                SignUpState.Error -> {
                    binding.layoutLoading.isVisible = false
                }
            }
        }

        viewModel.error.collectViewLifecycleAware { event ->
            event.getContentOrNull()?.let { showSnackbarWithText(it.textRes) }
        }
    }

    private fun setValidationErrors(error: SignUpState.ValidationError) {
        error.causes.forEach {
            when (it) {
                AuthInteractor.AuthParam.NAME -> {
                    binding.textInputName.error = getString(R.string.auth_name_requirements)
                    binding.textInputName.isErrorEnabled = true
                }
                AuthInteractor.AuthParam.EMAIL -> {
                    binding.textInputEmail.error = getString(R.string.auth_email_invalid)
                    binding.textInputEmail.isErrorEnabled = true
                }
                AuthInteractor.AuthParam.PASSWORD -> {
                    binding.textInputPassword.error = getString(R.string.auth_password_requirements)
                    binding.textInputPassword.isErrorEnabled = true
                }
            }
        }
    }

    private fun clearValidationErrors() {
        binding.textInputName.isErrorEnabled = false
        binding.textInputEmail.isErrorEnabled = false
        binding.textInputPassword.isErrorEnabled = false
    }

    private val SignUpError.textRes get() = when (this) {
        SignUpError.EmailOccupied -> R.string.auth_email_occupied
        is SignUpError.Common -> error.textRes
    }
}