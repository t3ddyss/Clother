package com.t3ddyss.clother.ui.sign_in

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.databinding.FragmentSignInBinding
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.models.domain.*
import com.t3ddyss.clother.ui.BaseFragment
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment
    : BaseFragment<FragmentSignInBinding>(FragmentSignInBinding::inflate) {
    private val viewModel by viewModels<SignInViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonSignIn.setOnClickListener {
            val email = binding.editTextEmail.text()
            val password = binding.editTextPassword.text()

            if (!email.validateEmail()) {
                binding.textInputEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputEmail.isErrorEnabled = false

            viewModel.signInWithCredentials(email, password)
        }

        binding.textViewResetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_resetPasswordFragment)
        }

        subscribeUi()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveEmail(binding.editTextEmail.text())
        viewModel.savePassword(binding.editTextPassword.text())
    }

    private fun subscribeUi() {
        viewModel.email.observe(viewLifecycleOwner, {
            binding.editTextEmail.text = it.toEditable()
        })

        viewModel.password.observe(viewLifecycleOwner, {
            binding.editTextPassword.text = it.toEditable()
        })

        viewModel.signInResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loading<*> ->
                    binding.layoutLoading.isVisible = true
                is Success<*> -> {
                    findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                }
                is Error<*> -> {
                    binding.layoutLoading.isVisible = false
                    showGenericMessage(it.message)
                }
                is Failed<*> -> {
                    binding.layoutLoading.isVisible = false
                    showGenericMessage(getString(R.string.no_connection))
                }
            }
        })
    }
}