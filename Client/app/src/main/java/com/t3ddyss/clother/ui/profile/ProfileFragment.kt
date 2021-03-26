package com.t3ddyss.clother.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding
import com.t3ddyss.clother.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private val profileViewModel by viewModels<ProfileViewModel>()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.buttonNewOffer.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_offerEditorFragment)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}