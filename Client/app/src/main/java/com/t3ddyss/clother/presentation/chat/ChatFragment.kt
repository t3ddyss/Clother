package com.t3ddyss.clother.presentation.chat

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentChatBinding
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.util.text
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.showSnackbarWithAction
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.IntentUtils
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val viewModel by viewModels<ChatViewModel>()

    private val args by navArgs<ChatFragmentArgs>()

    private val adapter = MessagesAdapter(this::onMessageClick, this::onImageClick)
    private var adapterDataObserver: RecyclerView.AdapterDataObserver? = null
    private var onScrollListener: RecyclerView.OnScrollListener? = null

    @Suppress("Deprecated")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Deprecated, but alternative requires API level 30
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            args.user.name,
            ToolbarUtils.NavIcon.UP
        )
        setHasOptionsMenu(true)

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                findNavController().navigate(
                    ChatFragmentDirections.actionChatFragmentToImageSelectorDialog()
                )
            } else {
                showSnackbarWithAction(
                    text = R.string.error_no_files_access,
                    actionText = R.string.action_grant_access,
                    action = { IntentUtils.openApplicationSettings(requireContext()) }
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
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_chat_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                findNavController().navigate(ChatFragmentDirections.actionChatFragmentToProfileFragment(args.user))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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

        setFragmentResultListener(ImageSelectorDialog.SELECTED_IMAGE_KEY) { _, bundle ->
            bundle.getParcelable<Uri>(ImageSelectorDialog.SELECTED_IMAGE_URI)?.let { uri ->
                viewModel.sendMessage(image = uri)
            }
        }

        setFragmentResultListener(MessageMenuDialog.SELECTED_ACTION_KEY) { _, bundle ->
            val action = bundle.getSerializable(MessageMenuDialog.SELECTED_ACTION) as? MessageMenuDialog.Action
            val messageId = bundle.getInt(MessageMenuDialog.MESSAGE_ID)

            if (action != null && messageId != 0) {
                when (action) {
                    MessageMenuDialog.Action.RETRY -> viewModel.retryToSendMessage(messageId)
                    MessageMenuDialog.Action.DELETE -> viewModel.deleteMessage(messageId)
                }
            }
        }
    }

    private fun onSendClick() {
        viewModel.sendMessage(body = binding.editTextMessage.text())
        binding.editTextMessage.text?.clear()
    }

    private fun onMessageClick(message: Message) {
        findNavController().navigate(
            ChatFragmentDirections.actionChatFragmentToMessageMenuDialog(
                messageId = message.localId,
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