package com.t3ddyss.clother.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.databinding.ListItemImageGalleryBinding

class GalleryImagesAdapter :
        ListAdapter<Uri, GalleryImagesAdapter.ImageViewHolder>(ImageDiffCallback()) {

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

    class ImageViewHolder(
            val binding: ListItemImageGalleryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(path: Uri) {
            Glide.with(binding.image)
                    .load(path)
                    .into(binding.image)
        }
    }
}