package com.t3ddyss.clother.ui.sign_in

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
import com.t3ddyss.clother.databinding.FragmentSignInBinding
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private val signInViewModel by viewModels<SignInViewModel>()

    private var _binding: FragmentSignInBinding? = null
    private val binding get() =  _binding!!

    private val navController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)

        signInViewModel.email.observe(viewLifecycleOwner, {
            binding.editTextEmail.text = it.toEditable()
        })

        signInViewModel.password.observe(viewLifecycleOwner, {
            binding.editTextPassword.text = it.toEditable()
        })

        signInViewModel.authTokens.observe(viewLifecycleOwner, {
            when(it) {
                is Loading<AuthTokens> ->
                    binding.frameLayoutLoading.isVisible = true
                is Success<AuthTokens> -> {
                    navController.navigate(R.id.action_signInFragment_to_homeFragment)
                }
                is Error<AuthTokens> -> {
                    binding.frameLayoutLoading.isVisible = false
                    (activity as? MainActivity)?.showGenericError(it.message)
                }
                is Failed<AuthTokens> -> {
                    binding.frameLayoutLoading.isVisible = false
                    (activity as? MainActivity)?.showConnectionError()
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignIn.setOnClickListener {
            val email = binding.editTextEmail.text()
            val password = binding.editTextPassword.text()

            if (!email.validateEmail()) {
                binding.textInputEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputEmail.isErrorEnabled = false

            signInViewModel.signInWithCredentials(email, password)
        }

        binding.textViewResetPassword.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_resetPasswordFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        signInViewModel.saveEmail(binding.editTextEmail.text())
        signInViewModel.savePassword(binding.editTextPassword.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}