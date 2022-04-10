package com.t3ddyss.clother.presentation.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.databinding.ListItemImageGalleryBinding
import com.t3ddyss.clother.domain.offers.models.MediaImage

class GalleryImagesAdapter(private val selectedLimitExceeded: () -> Unit) :
    ListAdapter<MediaImage, GalleryImagesAdapter.ImageViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ListItemImageGalleryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int = currentList.size

    inner class ImageViewHolder(
        val binding: ListItemImageGalleryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val image = getItem(absoluteAdapterPosition)

                if (!image.isSelected) {
                    if (currentList.count { it.isSelected } >= 5) {
                        selectedLimitExceeded.invoke()
                    } else {
                        binding.imageViewChecked.isVisible = true
                        image.isSelected = true
                    }
                } else {
                    binding.imageViewChecked.isVisible = false
                    image.isSelected = false
                }
            }
        }

        fun bind(mediaImage: MediaImage) {
            Glide.with(binding.image)
                .load(mediaImage.uri)
                .thumbnail(0.5f)
                .into(binding.image)

            binding.imageViewChecked.isVisible = mediaImage.isSelected
        }
    }

    private class ImageDiffCallback : DiffUtil.ItemCallback<MediaImage>() {
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
}