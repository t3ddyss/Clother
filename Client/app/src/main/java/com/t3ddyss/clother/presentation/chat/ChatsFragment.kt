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
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsFragment : BaseFragment<FragmentChatsBinding>(FragmentChatsBinding::inflate) {
    private val viewModel by viewModels<ChatsViewModel>()

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
                }
            }
        }
    }

    private fun onChatClick(chat: Chat) {
        val action = ChatsFragmentDirections
            .actionChatsFragmentToChatFragment(chat.interlocutor.toArg())
        findNavController().navigate(action)
    }
}