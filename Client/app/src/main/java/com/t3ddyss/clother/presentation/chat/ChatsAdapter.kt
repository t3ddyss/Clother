package com.t3ddyss.clother.presentation.chat

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemChatBinding
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.presentation.profile.AvatarLoader
import com.t3ddyss.clother.util.formatDate

class ChatsAdapter(
    private val clickListener: (Chat) -> Unit
) : ListAdapter<Chat, ChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            ListItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), clickListener
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(
        private val binding: ListItemChatBinding,
        private val clickListener: (Chat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                clickListener.invoke(getItem(absoluteAdapterPosition))
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(chat: Chat) {
            val context = binding.root.context
            AvatarLoader.loadAvatar(binding.avatar, chat.interlocutor.image, R.drawable.ic_avatar_default)
            binding.textViewName.text = chat.interlocutor.name
            binding.textViewTime.text = chat.lastMessage.createdAt.formatDate()

            val description = if (chat.lastMessage.isImage) {
                context.getString(R.string.chat_image_icon)
            } else {
                chat.lastMessage.body
            } ?: ""

            if (chat.lastMessage.isIncoming) {
                binding.textViewMessage.text = description
            } else {
                binding.textViewMessage.text = "${binding.root.context.getString(R.string.chat_sender_you)}: " +
                        description
            }
        }
    }

    private class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}