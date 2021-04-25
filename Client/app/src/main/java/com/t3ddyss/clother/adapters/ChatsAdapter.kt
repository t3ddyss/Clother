package com.t3ddyss.clother.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemChatBinding
import com.t3ddyss.clother.models.chat.ChatWithMessageAndUser
import com.t3ddyss.clother.utilities.formatDate

class ChatsAdapter(
        private val userId: Int,
        private val clickListener: (ChatWithMessageAndUser) -> Unit
) : ListAdapter<ChatWithMessageAndUser, ChatsAdapter.ChatViewHolder>(ChatWithMessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(ListItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ), clickListener)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(private val binding: ListItemChatBinding,
                               private val clickListener: (ChatWithMessageAndUser) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.invoke(getItem(absoluteAdapterPosition))
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(chat: ChatWithMessageAndUser) {
            with (binding) {
                textViewName.text = chat.interlocutorName
                textViewTime.text = chat.messageCreatedAt.formatDate()

                if (chat.messageUserId == userId) {
                    textViewMessage.text = "${binding.root.context.getString(R.string.you)}: " +
                            "${chat.messageBody}"
                }

                else {
                    textViewMessage.text = chat.messageBody
                }
            }
        }
    }
}