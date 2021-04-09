package com.t3ddyss.clother.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentMessagesBinding
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_AUTO
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MessagesFragment : Fragment() {

    private val viewModel by viewModels<MessagesViewModel>()
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)

        binding.buttonConnect.setOnClickListener {
            viewModel.getMessages()
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            Log.d(DEBUG_TAG, it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}