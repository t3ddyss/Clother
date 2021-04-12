package com.t3ddyss.clother.ui.chats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.t3ddyss.clother.adapters.ChatsAdapter
import com.t3ddyss.clother.databinding.FragmentChatsBinding
import com.t3ddyss.clother.models.Success
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsFragment : Fragment() {
    private val viewModel by viewModels<ChatsViewModel>()
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private val adapter = ChatsAdapter {
        // TODO
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        binding.listChats.adapter = adapter

        viewModel.chats.observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    adapter.submitList(it.content)
                }
                else -> {
                    Log.d(DEBUG_TAG, "Error getting chats")
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}