package com.t3ddyss.clother.ui.sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.t3ddyss.clother.databinding.FragmentSignUpBinding
import com.t3ddyss.clother.ui.utils.text
import com.t3ddyss.clother.ui.utils.toEditable
import com.t3ddyss.clother.ui.utils.validateName

class SignUpFragment : Fragment() {

    private val signUpViewModel by viewModels<SignUpViewModel>()
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        signUpViewModel.name.observe(viewLifecycleOwner,
                {
                    name -> name?.let {
                        binding.editTextSignUpName.text = name.toEditable()
                }
                })
        signUpViewModel.email.observe(viewLifecycleOwner,
                {email -> email?.let {
                    binding.editTextSignUpEmail.text = email.toEditable()
                }})
        signUpViewModel.password.observe(viewLifecycleOwner,
                {password -> password?.let {
                    binding.editTextSignUpPassword.text = password.toEditable()
                }})

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignUp.setOnClickListener(
                {
                }
        )

    }

    override fun onStop() {
        super.onStop()
        signUpViewModel.setName(binding.editTextSignUpName.text())
        signUpViewModel.setEmail(binding.editTextSignUpEmail.text())
        signUpViewModel.setPassword(binding.editTextSignUpPassword.text())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}