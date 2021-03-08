package com.t3ddyss.clother.ui.password_reset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.databinding.FragmentResetPasswordBinding
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.utilities.text
import com.t3ddyss.clother.utilities.toEditable
import com.t3ddyss.clother.utilities.validateEmail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {
    private val resetPasswordViewModel by viewModels<ResetPasswordViewModel>()

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy { findNavController() }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)

        resetPasswordViewModel.email.observe(viewLifecycleOwner) {
            binding.editTextResetPasswordEmail.text = it.toEditable()
        }

        resetPasswordViewModel.passwordResetResponse.observe(viewLifecycleOwner) {
            val response = it.getContentIfNotHandled() ?: return@observe

            when (response) {
                is Loading<PasswordResetResponse> ->
                    binding.frameLayoutResetPasswordLoading.visibility = View.VISIBLE
                is Success<PasswordResetResponse> -> {
                    navController
                            .navigate(ResetPasswordFragmentDirections
                            .actionResetPasswordFragmentToEmailActionFragment(
                                    getString(R.string.password_reset_message),
                                    response.content?.email ?: getString(R.string.your_email)))
                    binding.frameLayoutResetPasswordLoading.visibility = View.GONE
                }
                is Error<PasswordResetResponse> -> {
                    binding.frameLayoutResetPasswordLoading.visibility = View.GONE
                    Snackbar.make(binding.constraintLayoutResetPassword,
                            response.message ?:
                            getString(R.string.unknown_error),
                            Snackbar.LENGTH_SHORT).show()
                }
                is Failed<PasswordResetResponse> -> {
                    binding.frameLayoutResetPasswordLoading.visibility = View.GONE
                    Snackbar.make(binding.constraintLayoutResetPassword,
                            getString(R.string.no_connection),
                            Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextResetPasswordEmail.text()

            if (!email.validateEmail()) {
                binding.textInputResetPasswordEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputResetPasswordEmail.isErrorEnabled = false

            resetPasswordViewModel.resetPassword(email)
        }
    }

    override fun onPause() {
        super.onPause()
        resetPasswordViewModel.saveEmail(binding.editTextResetPasswordEmail.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}