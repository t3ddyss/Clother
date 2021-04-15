package com.t3ddyss.clother.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemChatBinding
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.utilities.formatDate
import com.t3ddyss.clother.utilities.getImageUrlForCurrentDevice

class ChatsAdapter(
        private val userId: Int,
    private val clickListener: (Chat) -> Unit
) : ListAdapter<Chat, ChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {

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
                         private val clickListener: (Chat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.invoke(getItem(absoluteAdapterPosition))
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(chat: Chat) {
            with (binding) {
                textViewName.text = chat.interlocutor.name
                textViewTime.text = chat.lastMessage.createdAt.formatDate()

                if (chat.lastMessage.userId == userId) {
                    textViewMessage.text = "${binding.root.context.getString(R.string.you)}: " +
                            "${chat.lastMessage.body}"
                }

                else {
                    textViewMessage.text = chat.lastMessage.body
                }

                chat.interlocutor.image?.let {
                    Glide.with(cardViewAvatar.imageViewAvatar)
                        .load(it.getImageUrlForCurrentDevice())
                        .centerCrop()
                        .into(cardViewAvatar.imageViewAvatar)
                }
            }
        }
    }
}