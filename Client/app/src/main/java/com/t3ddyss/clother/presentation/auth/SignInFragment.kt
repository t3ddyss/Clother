package com.t3ddyss.clother.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentSignInBinding
import com.t3ddyss.clother.util.text
import com.t3ddyss.clother.util.toEditable
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.StringUtils
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
            val email = binding.editTextEmail.text()
            val password = binding.editTextPassword.text()

            if (!StringUtils.isValidEmail(email)) {
                binding.textInputEmail.error = getString(R.string.auth_email_invalid)
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
        viewModel.email.observe(viewLifecycleOwner) {
            binding.editTextEmail.text = it.toEditable()
        }

        viewModel.password.observe(viewLifecycleOwner) {
            binding.editTextPassword.text = it.toEditable()
        }

        viewModel.signInResult.observe(viewLifecycleOwner) {
            when (it) {
                is Loading<*> ->
                    binding.layoutLoading.isVisible = true
                is Success<*> -> {
                    findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                }
                is Error<*> -> {
                    binding.layoutLoading.isVisible = false
                    showSnackbarWithText(it)
                }
            }
        }
    }
}