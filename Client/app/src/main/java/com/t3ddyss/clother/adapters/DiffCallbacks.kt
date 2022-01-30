package com.t3ddyss.clother.adapters

import androidx.recyclerview.widget.DiffUtil
import com.t3ddyss.clother.models.domain.Chat
import com.t3ddyss.clother.models.domain.MediaImage
import com.t3ddyss.clother.models.domain.Message
import com.t3ddyss.clother.models.domain.Offer
import com.t3ddyss.core.domain.Category

class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
    override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean {
        return oldItem == newItem
    }
}

class ImageDiffCallback : DiffUtil.ItemCallback<MediaImage>() {
    override fun areItemsTheSame(oldItem: MediaImage, newItem: MediaImage): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: MediaImage, newItem: MediaImage): Boolean {
        if (oldItem.isSelected) {
            newItem.isSelected = true
        }

        return oldItem == newItem
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
    override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
        return oldItem == newItem
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.localId == newItem.localId
                || oldItem.serverId == newItem.serverId
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}