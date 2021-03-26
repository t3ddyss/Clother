package com.t3ddyss.clother.ui.offer_editor

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding

class OfferEditorFragment : Fragment() {
    private val viewModel by viewModels<OfferEditorViewModel>()
    private var _binding: FragmentOfferEditorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}