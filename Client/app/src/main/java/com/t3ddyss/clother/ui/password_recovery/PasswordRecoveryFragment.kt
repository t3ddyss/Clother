package com.t3ddyss.clother.ui.password_recovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.databinding.FragmentPasswordRecoveryBinding
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.models.domain.*
import com.t3ddyss.clother.utilities.text
import com.t3ddyss.clother.utilities.toEditable
import com.t3ddyss.clother.utilities.validateEmail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class PasswordRecoveryFragment : Fragment() {
    private val viewModel by viewModels<PasswordRecoveryViewModel>()

    private var _binding: FragmentPasswordRecoveryBinding? = null
    private val binding get() = _binding!!

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordRecoveryBinding.inflate(inflater, container, false)

        viewModel.email.observe(viewLifecycleOwner) {
            binding.editTextEmail.text = it.toEditable()
        }

        viewModel.passwordRecoveryResult.observe(viewLifecycleOwner) {
            if (it.hasBeenHandled && it is Success<*>) return@observe

            when (val response = it.peekContent()) {
                is Loading<*> ->
                    binding.layoutLoading.isVisible = true
                is Success<Response> -> {
                    findNavController()
                        .navigate(
                            PasswordRecoveryFragmentDirections
                                .actionResetPasswordFragmentToEmailActionFragment(
                                    getString(R.string.password_reset_message),
                                    binding.editTextEmail.text()
                                )
                        )
                    binding.layoutLoading.isVisible = false
                }
                is Error<Response> -> {
                    binding.layoutLoading.isVisible = false
                    (activity as? MainActivity)?.showGenericMessage(response.message)
                }
                is Failed<Response> -> {
                    binding.layoutLoading.isVisible = false
                    (activity as? MainActivity)?.showGenericMessage(getString(R.string.no_connection))
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text()

            if (!email.validateEmail()) {
                binding.textInputEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputEmail.isErrorEnabled = false

            viewModel.resetPassword(email)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveEmail(binding.editTextEmail.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}