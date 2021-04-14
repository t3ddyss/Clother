package com.t3ddyss.clother.ui.password_recovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.databinding.FragmentPasswordRecoveryBinding
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.models.auth.AuthResponse
import com.t3ddyss.clother.models.common.Error
import com.t3ddyss.clother.models.common.Failed
import com.t3ddyss.clother.models.common.Loading
import com.t3ddyss.clother.models.common.Success
import com.t3ddyss.clother.utilities.text
import com.t3ddyss.clother.utilities.toEditable
import com.t3ddyss.clother.utilities.validateEmail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordRecoveryFragment : Fragment() {
    private val resetPasswordViewModel by viewModels<PasswordRecoveryViewModel>()

    private var _binding: FragmentPasswordRecoveryBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy { findNavController() }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordRecoveryBinding.inflate(inflater, container, false)

        resetPasswordViewModel.email.observe(viewLifecycleOwner) {
            binding.editTextEmail.text = it.toEditable()
        }

        resetPasswordViewModel.passwordResetResponse.observe(viewLifecycleOwner) {
            val response = it.getContentIfNotHandled() ?: return@observe

            when (response) {
                is Loading<AuthResponse> ->
                    (activity as? MainActivity)?.setLoadingVisibility(true)
                is Success<AuthResponse> -> {
                    navController
                            .navigate(PasswordRecoveryFragmentDirections
                            .actionResetPasswordFragmentToEmailActionFragment(
                                    getString(R.string.password_reset_message),
                                    response.content?.email ?: getString(R.string.your_email)))
                    (activity as? MainActivity)?.setLoadingVisibility(false)
                }
                is Error<AuthResponse> -> {
                    (activity as? MainActivity)?.setLoadingVisibility(false)
                    (activity as? MainActivity)?.showGenericError(response.message)
                }
                is Failed<AuthResponse> -> {
                    (activity as? MainActivity)?.setLoadingVisibility(false)
                    (activity as? MainActivity)?.showGenericError(getString(R.string.no_connection))
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text()

            if (!email.validateEmail()) {
                binding.textInputEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputEmail.isErrorEnabled = false

            resetPasswordViewModel.resetPassword(email)
        }
    }

    override fun onPause() {
        super.onPause()
        resetPasswordViewModel.saveEmail(binding.editTextEmail.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}