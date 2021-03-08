package com.t3ddyss.clother.ui.sign_in

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
            binding.editTextSignInEmail.text = it.toEditable()
        })

        signInViewModel.password.observe(viewLifecycleOwner, {
            binding.editTextSignInPassword.text = it.toEditable()
        })

        signInViewModel.signInResponse.observe(viewLifecycleOwner, {
            when(it) {
                is Loading<SignInResponse> ->
                    binding.frameLayoutSignInLoading.visibility = View.VISIBLE
                is Success<SignInResponse> -> {
                    navController.navigate(R.id.action_signInFragment_to_homeFragment)
                }
                is Error<SignInResponse> -> {
                    binding.frameLayoutSignInLoading.visibility = View.GONE
                    Snackbar.make(binding.constraintLayoutSignIn,
                            it.message ?:
                            getString(R.string.unknown_error),
                            Snackbar.LENGTH_SHORT).show()
                }
                is Failed<SignInResponse> -> {
                    binding.frameLayoutSignInLoading.visibility = View.GONE
                    Snackbar.make(binding.constraintLayoutSignIn,
                            getString(R.string.no_connection),
                            Snackbar.LENGTH_SHORT).show()
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignIn.setOnClickListener {
            val email = binding.editTextSignInEmail.text()
            val password = binding.editTextSignInPassword.text()

            if (!email.validateEmail()) {
                binding.textInputSignInEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputSignInEmail.isErrorEnabled = false

            signInViewModel.signInWithCredentials(email, password)
        }

        binding.textViewResetPassword.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_resetPasswordFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        signInViewModel.saveEmail(binding.editTextSignInEmail.text())
        signInViewModel.savePassword(binding.editTextSignInPassword.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}