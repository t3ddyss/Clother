package com.t3ddyss.clother.ui.sign_up

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
import com.t3ddyss.clother.databinding.FragmentSignUpBinding
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val signUpViewModel by viewModels<SignUpViewModel>()

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy {
        findNavController()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        signUpViewModel.name.observe(viewLifecycleOwner,
                {
                    it?.let {
                        binding.editTextName.text = it.toEditable()
                    }
                })
        signUpViewModel.email.observe(viewLifecycleOwner,
                {
                    it?.let {
                        binding.editTextEmail.text = it.toEditable()
                    }
                })
        signUpViewModel.password.observe(viewLifecycleOwner,
                {
                    it?.let {
                        binding.editTextPassword.text = it.toEditable()
                    }
                })

        signUpViewModel.authResponse.observe(viewLifecycleOwner,
                {
                    val response = it.getContentIfNotHandled() ?: return@observe

                    // TODO implement error messages localization on server side or in client
                    when(response){
                        is Loading<AuthResponse> ->
                            (activity as? MainActivity)?.setLoadingVisibility(true)
                        is Success<AuthResponse> -> {
                            navController.navigate(
                                SignUpFragmentDirections.actionSignUpFragmentToEmailActionFragment(
                                        getString(R.string.email_activation),
                                        response.content?.email ?: getString(R.string.your_email)))
                            (activity as? MainActivity)?.setLoadingVisibility(false)
                            signUpViewModel.clearCredentials()
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
                })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextName.text()
            val email = binding.editTextEmail.text()
            val password = binding.editTextPassword.text()

            if (!name.validateName()) {
                binding.textInputName.error = getString(R.string.name_requirements)
                return@setOnClickListener
            }
            binding.textInputName.isErrorEnabled = false

            if (!email.validateEmail()) {
                binding.textInputEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputEmail.isErrorEnabled = false

            if (!password.validatePassword()) {
                binding.textInputPassword.error = getString(R.string.password_requirements)
                return@setOnClickListener
            }
            binding.textInputPassword.isErrorEnabled = false

            signUpViewModel.createUserWithCredentials(name, email, password)
        }

        binding.textViewSignIn.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        signUpViewModel.saveName(binding.editTextName.text())
        signUpViewModel.saveEmail(binding.editTextName.text())
        signUpViewModel.savePassword(binding.editTextName.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}