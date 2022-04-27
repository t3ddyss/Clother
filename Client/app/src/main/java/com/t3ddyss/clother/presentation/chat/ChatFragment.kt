package com.t3ddyss.clother.presentation.chat

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentChatBinding
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.util.text
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.showSnackbarWithText
import com.t3ddyss.navigation.util.observeNavigationResultOnce
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val viewModel by viewModels<ChatViewModel>()

    private val adapter = MessagesAdapter {
        findNavController()
            .navigate(
                ChatFragmentDirections.actionChatFragmentToImageFragment(it.image ?: "")
            )
    }
    private lateinit var adapterDataObserver: RecyclerView.AdapterDataObserver
    private lateinit var onScrollListener: RecyclerView.OnScrollListener

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
                findNavController().navigate(R.id.action_chatFragment_to_imagesDialogFragment)
            }
        }
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    binding.listMessages.scrollToPosition(positionStart)
                }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)

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
                        viewModel.requestMessages()
                    }
                }
            }
        }
        binding.listMessages.addOnScrollListener(onScrollListener)

        binding.buttonSend.setOnClickListener {
            viewModel.sendMessage(body = binding.editTextMessage.text())
            binding.editTextMessage.text?.clear()
        }
        binding.buttonAttach.setOnClickListener {
            observeNavigationResultOnce<String>(ImageSelectorDialog.SELECTED_IMAGE) {
                viewModel.sendMessage(image = it)
            }
            requestGalleryPermissionLauncher?.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        subscribeUi()
    }

    override fun onDestroyView() {
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
        binding.listMessages.removeOnScrollListener(onScrollListener)
        super.onDestroyView()
    }

    private fun subscribeUi() {
        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.emptyState.isVisible = it.isEmpty()
        }

        viewModel.loadStatus.observe(viewLifecycleOwner) {
            when (it) {
                is LoadResult.Success -> {
                    viewModel.isEndOfPaginationReached = it.isEndOfPaginationReached
                }

                is LoadResult.Error -> {
                    showSnackbarWithText(it.exception)
                }
            }
        }
    }

    companion object {
        const val RECYCLER_THRESHOLD = 10
    }
}