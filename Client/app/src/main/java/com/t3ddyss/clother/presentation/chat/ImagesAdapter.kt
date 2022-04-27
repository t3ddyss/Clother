package com.t3ddyss.clother.presentation.chat

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.databinding.ListItemImageGalleryBinding

class ImagesAdapter(
    val onClickListener: (Uri) -> Unit
) : ListAdapter<Uri, ImagesAdapter.ImageViewHolder>(UriDiffCallback()) {

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

    inner class ImageViewHolder(
        val binding: ListItemImageGalleryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onClickListener.invoke(getItem(absoluteAdapterPosition))
            }
        }

        fun bind(uri: Uri) {
            Glide.with(binding.image)
                .load(uri)
                .thumbnail(0.5f)
                .into(binding.image)
        }
    }

    private class UriDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
}