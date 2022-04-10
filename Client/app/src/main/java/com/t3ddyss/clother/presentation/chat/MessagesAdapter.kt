package com.t3ddyss.clother.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemMessageInBinding
import com.t3ddyss.clother.databinding.ListItemMessageOutBinding
import com.t3ddyss.clother.domain.models.Message
import com.t3ddyss.clother.domain.models.MessageStatus
import com.t3ddyss.clother.util.formatTime

class MessagesAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.list_item_message_in -> {
                IncomingMessageViewHolder(
                    ListItemMessageInBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            R.layout.list_item_message_out -> {
                OutgoingMessageViewHolder(
                    ListItemMessageOutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IncomingMessageViewHolder -> holder.bind(getItem(position))
            is OutgoingMessageViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isIncoming) R.layout.list_item_message_in
        else R.layout.list_item_message_out
    }

    class IncomingMessageViewHolder(
        private val binding: ListItemMessageInBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewBody.requestLayout()

            binding.textViewBody.text = message.body
            binding.textViewTime.text = message.createdAt.formatTime()
        }
    }

    class OutgoingMessageViewHolder(
        private val binding: ListItemMessageOutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewBody.requestLayout()
            binding.textViewTime.requestLayout()
            binding.imageViewStatus.requestLayout()

            binding.textViewBody.text = message.body

            when (message.status) {
                MessageStatus.DELIVERED -> {
                    binding.textViewTime.text = message.createdAt.formatTime()
                    binding.textViewTime.isVisible = true
                    binding.imageViewStatus.isVisible = false
                }

                MessageStatus.DELIVERING -> {
                    binding.imageViewStatus.setImageResource(R.drawable.ic_schedule)
                    binding.imageViewStatus.isVisible = true
                    binding.textViewTime.isVisible = false
                }

                MessageStatus.FAILED -> {
                    binding.imageViewStatus.setImageResource(R.drawable.ic_error)
                    binding.imageViewStatus.isVisible = true
                    binding.textViewTime.isVisible = false
                }
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.localId == newItem.localId
                    || oldItem.serverId == newItem.serverId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}