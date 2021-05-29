package com.t3ddyss.clother.ui.sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.databinding.FragmentSignUpBinding
import com.t3ddyss.clother.models.domain.*
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val viewModel by viewModels<SignUpViewModel>()

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextName.text()
            val email = binding.editTextEmail.text()
            val password = binding.editTextPassword.text()

            viewModel.createUserWithCredentials(name, email, password)
        }

        binding.textViewSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        viewModel.name.observe(viewLifecycleOwner,
            {
                it?.let {
                    binding.editTextName.text = it.toEditable()
                }
            })
        viewModel.email.observe(viewLifecycleOwner,
            {
                it?.let {
                    binding.editTextEmail.text = it.toEditable()
                }
            })
        viewModel.password.observe(viewLifecycleOwner,
            {
                it?.let {
                    binding.editTextPassword.text = it.toEditable()
                }
            })

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

        // TODO implement error messages localization on server side or in client
        viewModel.signUpResult.observe(viewLifecycleOwner,
            {
                if (it.hasBeenHandled && it is Success<*>) return@observe

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
                        (activity as? MainActivity)?.showGenericMessage(
                            response.message
                        )
                    }
                    is Failed<*> -> {
                        binding.layoutLoading.isVisible = false
                        (activity as? MainActivity)?.showGenericMessage(
                            getString(R.string.no_connection)
                        )
                    }
                }
            })

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveName(binding.editTextName.text())
        viewModel.saveEmail(binding.editTextEmail.text())
        viewModel.savePassword(binding.editTextPassword.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}