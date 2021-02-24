package com.t3ddyss.clother.ui.sign_up

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
import com.t3ddyss.clother.data.SignUpResponse
import com.t3ddyss.clother.databinding.FragmentSignUpBinding
import com.t3ddyss.clother.utilities.*

class SignUpFragment : Fragment() {

    private val signUpViewModel by viewModels<SignUpViewModel>()

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy {
        NavHostFragment.findNavController(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        signUpViewModel.name.observe(viewLifecycleOwner,
                {
                    it?.let {
                        binding.editTextSignUpName.text = it.toEditable()
                    }
                })
        signUpViewModel.email.observe(viewLifecycleOwner,
                {
                    it?.let {
                        binding.editTextSignUpEmail.text = it.toEditable()
                    }
                })
        signUpViewModel.password.observe(viewLifecycleOwner,
                {
                    it?.let {
                        binding.editTextSignUpPassword.text = it.toEditable()
                    }
                })

        signUpViewModel.signUpResponse.observe(viewLifecycleOwner,
                {
                    val response = it.getContentIfNotHandled() ?: return@observe

                    // TODO implement error messages localization on server side or in client
                    when(response){
                        is Loading<SignUpResponse> ->
                            binding.frameLayoutSignUpLoading.visibility = View.VISIBLE
                        is Success<SignUpResponse> -> {
                            navController.navigate(
                                SignUpFragmentDirections.actionSignUpFragmentToEmailSentFragment(
                                        getString(R.string.email_activation) + " ",
                                        response.data?.email ?: getString(R.string.your_email)))
                            binding.frameLayoutSignUpLoading.visibility = View.GONE
                            signUpViewModel.clearCredentials()
                        }
                        is Error<SignUpResponse> -> {
                            binding.frameLayoutSignUpLoading.visibility = View.GONE
                            Snackbar.make(binding.constraintLayoutSignUp,
                                    response.message ?:
                                    getString(R.string.unknown_error),
                                    Snackbar.LENGTH_SHORT).show()
                        }
                        is Failed<SignUpResponse> -> {
                            binding.frameLayoutSignUpLoading.visibility = View.GONE
                            Snackbar.make(binding.constraintLayoutSignUp,
                                    getString(R.string.no_connection),
                                    Snackbar.LENGTH_SHORT).show()
                        }
                    }
                })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextSignUpName.text()
            val email = binding.editTextSignUpEmail.text()
            val password = binding.editTextSignUpPassword.text()

            if (!name.validateName()) {
                binding.textInputSignUpName.error = getString(R.string.name_requirements)
                return@setOnClickListener
            }
            binding.textInputSignUpName.isErrorEnabled = false

            if (!email.validateEmail()) {
                binding.textInputSignUpEmail.error = getString(R.string.email_invalid)
                return@setOnClickListener
            }
            binding.textInputSignUpEmail.isErrorEnabled = false

            if (!password.validatePassword()) {
                binding.textInputSignUpPassword.error = getString(R.string.password_requirements)
                return@setOnClickListener
            }
            binding.textInputSignUpPassword.isErrorEnabled = false

            signUpViewModel.createUserWithCredentials(name, email, password)
        }

        binding.textViewSignIn.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        signUpViewModel.saveName(binding.editTextSignUpName.text())
        signUpViewModel.saveEmail(binding.editTextSignUpEmail.text())
        signUpViewModel.savePassword(binding.editTextSignUpPassword.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}