package com.t3ddyss.clother.ui.chats

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.adapters.ChatsAdapter
import com.t3ddyss.clother.databinding.FragmentChatsBinding
import com.t3ddyss.clother.models.domain.Loading
import com.t3ddyss.clother.utilities.CURRENT_USER_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatsFragment : Fragment() {
    private val viewModel by viewModels<ChatsViewModel>()
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var prefs: SharedPreferences

    private val adapter by lazy {
        ChatsAdapter(prefs.getInt(CURRENT_USER_ID, 0)) {
            val action = ChatsFragmentDirections
                .actionChatsFragmentToChatFragment(it.interlocutorId, it.interlocutorName)
            findNavController().navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        binding.listChats.adapter = adapter

        viewModel.chats.observe(viewLifecycleOwner) {
            adapter.submitList(it.content)

            binding.emptyState.isVisible = it.content?.isEmpty() == true
            binding.layoutLoading.isVisible = it is Loading && it.content.isNullOrEmpty()
        }

        viewModel.getChats()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}