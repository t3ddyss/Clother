package com.t3ddyss.clother.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemImageAddBinding
import com.t3ddyss.clother.databinding.ListItemImageEditorBinding
import com.t3ddyss.clother.models.GalleryImage

class OfferEditorImagesAdapter(
        private val images: MutableList<GalleryImage?>,
        private val attachImageListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        images.add(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.list_item_image_add) {
            AttachImageViewHolder(ListItemImageAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false), attachImageListener)
        }
        else {
            ImageViewHolder(ListItemImageEditorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AttachImageViewHolder -> { }
            is ImageViewHolder -> images[position]?.let { holder.bind(it) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val image = images[position]

        return if (image == null) R.layout.list_item_image_add
        else R.layout.list_item_image_editor
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(
            val binding: ListItemImageEditorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonRemove.setOnClickListener {
                val position = absoluteAdapterPosition
                images[position]?.isSelected = false
                images.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        fun bind(image: GalleryImage) {
            Glide.with(binding.image)
                    .load(image.uri)
                    .into(binding.image)
        }
    }

    class AttachImageViewHolder(
            binding: ListItemImageAddBinding,
            clickListener: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                clickListener.invoke()
            }
        }
    }
}
