package com.t3ddyss.clother.presentation.chat

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.common.common.Mappers.toArg
import com.t3ddyss.clother.databinding.FragmentChatsBinding
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.ChatsState
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsListFragment : BaseFragment<FragmentChatsBinding>(FragmentChatsBinding::inflate) {
    private val viewModel by viewModels<ChatsListViewModel>()

    private val adapter = ChatsAdapter(this::onChatClick)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.menu_messages)
        )
        binding.listChats.adapter = adapter
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.chats.collectViewLifecycleAware { state ->
            adapter.submitList(state.chats)

            when (state) {
                is ChatsState.Cache -> {
                    binding.listChats.isVisible = state.chats.isNotEmpty()
                    binding.layoutLoading.isVisible = state.chats.isEmpty()
                }
                is ChatsState.Fetched -> {
                    binding.listChats.isVisible = state.chats.isNotEmpty()
                    binding.layoutLoading.isVisible = false
                    binding.emptyState.isVisible = state.chats.isEmpty()
                }
                is ChatsState.Error -> {
                    binding.listChats.isVisible = true
                    binding.layoutLoading.isVisible = false
                }
            }
        }
    }

    private fun onChatClick(chat: Chat) {
        val action = ChatsListFragmentDirections
            .actionChatsFragmentToChatFragment(chat.interlocutor.toArg())
        findNavController().navigate(action)
    }
}