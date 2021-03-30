package com.t3ddyss.clother.adapters

import androidx.recyclerview.widget.DiffUtil
import com.t3ddyss.clother.models.Category
import com.t3ddyss.clother.models.GalleryImage
import com.t3ddyss.clother.models.Offer

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