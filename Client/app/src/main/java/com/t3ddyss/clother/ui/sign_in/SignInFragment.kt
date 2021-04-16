package com.t3ddyss.clother.ui.sign_in

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
import com.t3ddyss.clother.databinding.FragmentSignInBinding
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.models.auth.AuthData
import com.t3ddyss.clother.models.common.Error
import com.t3ddyss.clother.models.common.Failed
import com.t3ddyss.clother.models.common.Loading
import com.t3ddyss.clother.models.common.Success
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private val signInViewModel by viewModels<SignInViewModel>()

    private var _binding: FragmentSignInBinding? = null
    private val binding get() =  _binding!!

    private val navController by lazy {
        findNavController()
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)

        signInViewModel.email.observe(viewLifecycleOwner, {
            binding.editTextEmail.text = it.toEditable()
        })

        signInViewModel.password.observe(viewLifecycleOwner, {
            binding.editTextPassword.text = it.toEditable()
        })

        signInViewModel.authData.observe(viewLifecycleOwner, {
            when(it) {
                is Loading<AuthData> ->
                    (activity as? MainActivity)?.setLoadingVisibility(true)
                is Success<AuthData> -> {
                    navController.navigate(R.id.action_signInFragment_to_homeFragment)
                }
                is Error<AuthData> -> {
                    (activity as? MainActivity)?.setLoadingVisibility(false)
                    (activity as? MainActivity)?.showGenericMessage(it.message)
                }
                is Failed<AuthData> -> {
                    (activity as? MainActivity)?.setLoadingVisibility(false)
                    (activity as? MainActivity)?.showGenericMessage(getString(R.string.no_connection))
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