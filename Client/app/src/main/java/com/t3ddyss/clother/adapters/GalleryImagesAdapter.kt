package com.t3ddyss.clother.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.databinding.ListItemImageGalleryBinding

class GalleryImagesAdapter(private val images: List<Uri>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageViewHolder(ListItemImageGalleryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ImageViewHolder)?.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

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