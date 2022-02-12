package com.t3ddyss.clother.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemChatBinding
import com.t3ddyss.clother.models.domain.Chat
import com.t3ddyss.clother.utilities.formatDate

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
            with(binding) {
                textViewName.text = chat.interlocutor.name
                textViewTime.text = chat.lastMessage.createdAt.formatDate()

                if (chat.lastMessage.isIncoming) {
                    textViewMessage.text = chat.lastMessage.body
                } else {
                    textViewMessage.text = "${binding.root.context.getString(R.string.you)}: " +
                            "${chat.lastMessage.body}"
                }
            }
        }
    }
}