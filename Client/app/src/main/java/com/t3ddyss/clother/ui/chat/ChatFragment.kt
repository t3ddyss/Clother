package com.t3ddyss.clother.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.adapters.MessagesAdapter
import com.t3ddyss.clother.databinding.FragmentChatBinding
import com.t3ddyss.clother.models.common.LoadResult
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.text
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChatFragment : Fragment() {

    private val viewModel by viewModels<ChatViewModel>()

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ChatFragmentArgs>()

    private val adapter by lazy {
        MessagesAdapter(args.userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Deprecated, but alternative requires API level 30
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    binding.listMessages.scrollToPosition(positionStart)
                }
            }
        })
        binding.listMessages.layoutManager = layoutManager
        binding.listMessages.adapter = adapter
        binding.listMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                if (dy < 0) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) + RECYCLER_THRESHOLD
                            >= totalItemCount) {
                        viewModel.getMoreMessages(args.userId)
                    }
                }
            }
        })

        binding.buttonSend.setOnClickListener {
            viewModel.sendMessage(binding.editTextMessage.text(), args.userId)
            binding.editTextMessage.text?.clear()
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.loadStatus.observe(viewLifecycleOwner) {
            when (it) {
                is LoadResult.Success -> {
                    viewModel.isEndOfPaginationReached = it.isEndOfPaginationReached
                }

                else -> Log.d(DEBUG_TAG, "LoadStatus error")
            }
        }

        viewModel.getMessages(args.userId)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val RECYCLER_THRESHOLD = 10
    }
}