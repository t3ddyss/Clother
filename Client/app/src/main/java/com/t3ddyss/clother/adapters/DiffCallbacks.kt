package com.t3ddyss.clother.adapters

import androidx.recyclerview.widget.DiffUtil
import com.t3ddyss.clother.models.chat.ChatWithMessageAndUser
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.offers.Category
import com.t3ddyss.clother.models.common.GalleryImage
import com.t3ddyss.clother.models.offers.Offer

class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
    override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean {
        return oldItem == newItem
    }
}

class ImageDiffCallback : DiffUtil.ItemCallback<GalleryImage>() {
    override fun areItemsTheSame(oldItem: GalleryImage, newItem: GalleryImage): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: GalleryImage, newItem: GalleryImage): Boolean {
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

class ChatWithMessageDiffCallback : DiffUtil.ItemCallback<ChatWithMessageAndUser>() {
    override fun areItemsTheSame(oldItem: ChatWithMessageAndUser,
                                 newItem: ChatWithMessageAndUser): Boolean {
        return oldItem.localChatId == newItem.localChatId
    }

    override fun areContentsTheSame(oldItem: ChatWithMessageAndUser,
                                    newItem: ChatWithMessageAndUser): Boolean {
        return oldItem == newItem
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.localId == newItem.localId || oldItem.serverId == newItem.serverId
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}