package com.t3ddyss.clother.ui.sign_in

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.clother.R
import com.t3ddyss.clother.api.Error
import com.t3ddyss.clother.api.Failed
import com.t3ddyss.clother.api.Loading
import com.t3ddyss.clother.api.Success
import com.t3ddyss.clother.data.SignInResponse
import com.t3ddyss.clother.databinding.FragmentSignInBinding
import com.t3ddyss.clother.utilities.*

class SignInFragment : Fragment() {
    private val signInViewModel by viewModels<SignInViewModel>()

    private var _binding: FragmentSignInBinding? = null
    private val binding get() =  _binding!!

    private val navController by lazy {
        NavHostFragment.findNavController(this)
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
                    saveTokens(it)
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

    private fun saveTokens(response: Success<SignInResponse>) {
        val sp = activity?.getPreferences(Context.MODE_PRIVATE)
        sp?.edit()?.putString(ACCESS_TOKEN, response.data?.accessToken)?.apply()
        sp?.edit()?.putString(REFRESH_TOKEN, response.data?.refreshToken)?.apply()
        sp?.edit()?.putBoolean(AUTHENTICATED, true)?.apply()
    }
}