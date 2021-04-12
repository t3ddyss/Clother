package com.t3ddyss.clother.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.databinding.ListItemImageGalleryBinding
import com.t3ddyss.clother.models.common.GalleryImage

class GalleryImagesAdapter(private val selectedLimitExceeded: () -> Unit) :
        ListAdapter<GalleryImage, GalleryImagesAdapter.ImageViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(ListItemImageGalleryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false))
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
                    }
                    else {
                        binding.imageViewChecked.isVisible = true
                        image.isSelected = true
                    }
                }
                else {
                    binding.imageViewChecked.isVisible = false
                    image.isSelected = false
                }
            }
        }

        fun bind(image: GalleryImage) {
            Glide.with(binding.image)
                    .load(image.uri)
                    .into(binding.image)

            binding.imageViewChecked.isVisible = image.isSelected
        }
    }
}