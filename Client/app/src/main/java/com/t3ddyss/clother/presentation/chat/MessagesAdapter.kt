package com.t3ddyss.clother.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemImageInBinding
import com.t3ddyss.clother.databinding.ListItemImageOutBinding
import com.t3ddyss.clother.databinding.ListItemMessageInBinding
import com.t3ddyss.clother.databinding.ListItemMessageOutBinding
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.util.formatTime

class MessagesAdapter(
    private val clickListener: (Message) -> Unit
): ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.list_item_message_in -> {
                IncomingMessageViewHolder(
                    ListItemMessageInBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }

            R.layout.list_item_message_out -> {
                OutgoingMessageViewHolder(
                    ListItemMessageOutBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }

            R.layout.list_item_image_in -> {
                IncomingImageViewHolder(
                    ListItemImageInBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }

            R.layout.list_item_image_out -> {
                OutgoingImageViewHolder(
                    ListItemImageOutBinding.inflate(
                        inflater,
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
            is IncomingImageViewHolder -> holder.bind(getItem(position))
            is OutgoingImageViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when {
            message.isImage -> if (message.isIncoming) R.layout.list_item_image_in else R.layout.list_item_image_out
            else -> if (message.isIncoming) R.layout.list_item_message_in else R.layout.list_item_message_out
        }
    }

    class IncomingMessageViewHolder(
        private val binding: ListItemMessageInBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.body.requestLayout()

            binding.body.text = message.body
            binding.time.text = message.createdAt.formatTime()
        }
    }

    inner class OutgoingMessageViewHolder(
        private val binding: ListItemMessageOutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.body.requestLayout()
            binding.time.requestLayout()
            binding.status.requestLayout()

            binding.body.text = message.body
            setOutgoingMessageStatus(message, binding.status, binding.time)
        }
    }

    inner class IncomingImageViewHolder(
        private val binding: ListItemImageInBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                clickListener.invoke(getItem(absoluteAdapterPosition))
            }
        }

        fun bind(message: Message) {
            binding.time.text = message.createdAt.formatTime()
            Glide.with(binding.image)
                .load(message.image)
                .thumbnail(0.5f)
                .into(binding.image)
        }
    }

    inner class OutgoingImageViewHolder(
        private val binding: ListItemImageOutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                clickListener.invoke(getItem(absoluteAdapterPosition))
            }
        }

        fun bind(message: Message) {
            setOutgoingMessageStatus(message, binding.status, binding.time)
            Glide.with(binding.image)
                .load(message.image)
                .placeholder(R.drawable.placeholder_offer_image)
                .thumbnail(0.5f)
                .into(binding.image)
        }
    }

    private fun setOutgoingMessageStatus(message: Message, status: ImageView, time: TextView) {
        when (message.status) {
            MessageStatus.DELIVERED -> {
                time.text = message.createdAt.formatTime()
                time.isVisible = true
                status.isVisible = false
            }

            MessageStatus.DELIVERING -> {
                status.setImageResource(R.drawable.ic_schedule)
                status.isVisible = true
                time.isVisible = false
            }

            MessageStatus.FAILED -> {
                status.setImageResource(R.drawable.ic_error)
                status.isVisible = true
                time.isVisible = false
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