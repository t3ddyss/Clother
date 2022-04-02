package com.t3ddyss.clother.presentation.auth

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentSignUpBinding
import com.t3ddyss.clother.util.text
import com.t3ddyss.clother.util.toEditable
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.showSnackbarWithText
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
            val name = binding.editTextName.text()
            val email = binding.editTextEmail.text()
            val password = binding.editTextPassword.text()

            viewModel.createUserWithCredentials(name, email, password)
        }

        binding.textViewSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        subscribeUi()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveName(binding.editTextName.text())
        viewModel.saveEmail(binding.editTextEmail.text())
        viewModel.savePassword(binding.editTextPassword.text())
    }

    private fun subscribeUi() {
        viewModel.name.observe(viewLifecycleOwner
        ) {
            it?.let {
                binding.editTextName.text = it.toEditable()
            }
        }
        viewModel.email.observe(viewLifecycleOwner
        ) {
            it?.let {
                binding.editTextEmail.text = it.toEditable()
            }
        }
        viewModel.password.observe(viewLifecycleOwner
        ) {
            it?.let {
                binding.editTextPassword.text = it.toEditable()
            }
        }

        viewModel.nameError.observe(viewLifecycleOwner) {
            binding.textInputName.error = getString(R.string.name_requirements)
            binding.textInputName.isErrorEnabled = it
        }

        viewModel.emailError.observe(viewLifecycleOwner) {
            binding.textInputEmail.error = getString(R.string.email_invalid)
            binding.textInputEmail.isErrorEnabled = it
        }

        viewModel.passwordError.observe(viewLifecycleOwner) {
            binding.textInputPassword.error = getString(R.string.password_requirements)
            binding.textInputPassword.isErrorEnabled = it
        }

        viewModel.signUpResult.observe(viewLifecycleOwner
        ) {
            if (it.hasBeenHandled && it.peekContent() is Success<*>) return@observe

            when (val response = it.getContentIfNotHandled()
                ?: return@observe) {
                is Loading<*> ->
                    binding.layoutLoading.isVisible = true
                is Success<*> -> {
                    findNavController().navigate(
                        SignUpFragmentDirections.actionSignUpFragmentToEmailActionFragment(
                            getString(R.string.email_activation),
                            binding.editTextEmail.text()
                        )
                    )
                    binding.layoutLoading.isVisible = false
                    viewModel.clearCredentials()
                }
                is Error<*> -> {
                    binding.layoutLoading.isVisible = false
                    showSnackbarWithText(response.message)
                }
            }
        }
    }
}