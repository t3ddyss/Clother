package com.t3ddyss.clother.presentation.chat

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.databinding.FragmentChatBinding
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.util.text
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.Utils.asExpression
import com.t3ddyss.core.util.showSnackbarWithText
import com.t3ddyss.navigation.util.observeNavigationResultOnce
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val viewModel by viewModels<ChatViewModel>()

    private val adapter = MessagesAdapter(this::onMessageClick, this::onImageClick)
    private var adapterDataObserver: RecyclerView.AdapterDataObserver? = null
    private var onScrollListener: RecyclerView.OnScrollListener? = null

    private var requestGalleryPermissionLauncher: ActivityResultLauncher<String>? = null

    @Suppress("Deprecated")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Deprecated, but alternative requires API level 30
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requestGalleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                findNavController().navigate(
                    ChatFragmentDirections.actionChatFragmentToImageSelectorDialog()
                )
            }
        }
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    binding.listMessages.scrollToPosition(positionStart)
                }
            }
        }.also { adapter.registerAdapterDataObserver(it) }

        binding.listMessages.layoutManager = layoutManager
        binding.listMessages.adapter = adapter

        onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) + RECYCLER_THRESHOLD
                        >= totalItemCount
                    ) {
                        // TODO add loading indicator (header) like on home fragment
                        viewModel.onListEndReached()
                    }
                }
            }
        }.also {
            binding.listMessages.addOnScrollListener(it)
        }

        binding.buttonSend.setOnClickListener {
            onSendClick()
        }
        binding.buttonAttach.setOnClickListener {
            // FIXME if configuration change occurs while ImageSelectorDialog is on top, result won't be saved
            observeNavigationResultOnce<String>(ImageSelectorDialog.SELECTED_IMAGE) {
                viewModel.sendMessage(image = it)
            }
            requestGalleryPermissionLauncher?.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        binding.editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onSendClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        subscribeUi()
    }

    override fun onDestroyView() {
        adapterDataObserver?.let { adapter.unregisterAdapterDataObserver(it) }
        onScrollListener?.let { binding.listMessages.removeOnScrollListener(it) }
        adapterDataObserver = null
        onScrollListener = null
        super.onDestroyView()
    }

    private fun subscribeUi() {
        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.emptyState.isVisible = it.isEmpty()
        }

        viewModel.loadStatus.observe(viewLifecycleOwner) {
            when (it) {
                is LoadResult.Success -> Unit
                is LoadResult.Error -> showSnackbarWithText(it.exception)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.layoutLoading.isVisible = it
        }
    }

    private fun onSendClick() {
        viewModel.sendMessage(body = binding.editTextMessage.text())
        binding.editTextMessage.text?.clear()
    }

    private fun onMessageClick(message: Message) {
        observeNavigationResultOnce<String>(MessageMenuDialog.SELECTED_ACTION) {
            val action = MessageMenuDialog.Action.valueOf(it)
            when (action) {
                MessageMenuDialog.Action.RETRY -> viewModel.retryToSendMessage(message)
                MessageMenuDialog.Action.DELETE -> viewModel.deleteMessage(message)
            }.asExpression
        }
        findNavController().navigate(
            ChatFragmentDirections.actionChatFragmentToMessageMenuDialog(
                isRetryVisible = message.status == MessageStatus.FAILED
            )
        )
    }

    private fun onImageClick(message: Message) {
        findNavController().navigate(
            ChatFragmentDirections.actionChatFragmentToImageFragment(message.image ?: "")
        )
    }

    companion object {
        const val RECYCLER_THRESHOLD = 10
    }
}