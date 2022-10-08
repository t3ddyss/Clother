package com.t3ddyss.clother.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.t3ddyss.clother.databinding.DialogMessageMenuBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageMenuDialog : BottomSheetDialogFragment() {

    private var _binding: DialogMessageMenuBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MessageMenuDialogArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMessageMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.retry.isVisible = args.isRetryVisible
        binding.retry.setOnClickListener {
            setFragmentResult(
                SELECTED_ACTION_KEY,
                bundleOf(
                    SELECTED_ACTION to Action.RETRY,
                    MESSAGE_ID to args.messageId
                )
            )
            findNavController().popBackStack()
        }
        binding.delete.setOnClickListener {
            setFragmentResult(
                SELECTED_ACTION_KEY,
                bundleOf(
                    SELECTED_ACTION to Action.DELETE,
                    MESSAGE_ID to args.messageId
                )
            )
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class Action {
        RETRY,
        DELETE
    }

    companion object {
        const val SELECTED_ACTION_KEY = "selected_action_key"
        const val SELECTED_ACTION = "selected_action"
        const val MESSAGE_ID = "message_id"
    }
}