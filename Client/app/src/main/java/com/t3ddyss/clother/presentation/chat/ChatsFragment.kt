package com.t3ddyss.clother.presentation.chat

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.databinding.FragmentChatsBinding
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsFragment : BaseFragment<FragmentChatsBinding>(FragmentChatsBinding::inflate) {
    private val viewModel by viewModels<ChatsViewModel>()

    private val adapter = ChatsAdapter {
        val action = ChatsFragmentDirections
            .actionChatsFragmentToChatFragment(it.interlocutor)
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.listChats.adapter = adapter
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.chats.observe(viewLifecycleOwner) {
            when (it) {
                is Loading -> {
                    binding.layoutLoading.isVisible = it.content.isNullOrEmpty()
                    adapter.submitList(it.content)
                }
                is Success -> {
                    binding.layoutLoading.isVisible = false
                    binding.emptyState.isVisible = it.content?.isEmpty() == true
                    adapter.submitList(it.content)
                }
                is Error -> {
                    binding.layoutLoading.isVisible = false
                    binding.layoutLoading.isVisible = false
                }
            }
        }
    }
}