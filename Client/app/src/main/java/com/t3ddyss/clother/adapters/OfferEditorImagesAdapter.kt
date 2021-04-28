package com.t3ddyss.clother.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemImageAddBinding
import com.t3ddyss.clother.databinding.ListItemImageEditorBinding

class OfferEditorImagesAdapter(
    private val images: MutableList<Uri>,
    private val attachImageListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.list_item_image_add) {
            AttachImageViewHolder(
                ListItemImageAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), attachImageListener
            )
        } else {
            ImageViewHolder(
                ListItemImageEditorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AttachImageViewHolder -> {
            }
            is ImageViewHolder -> holder.bind(images[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= images.size) R.layout.list_item_image_add
        else R.layout.list_item_image_editor
    }

    override fun getItemCount() = MAX_SIZE

    inner class ImageViewHolder(
        val binding: ListItemImageEditorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonRemove.setOnClickListener {
                val position = absoluteAdapterPosition
                images.removeAt(position)

                notifyItemRemoved(position)
                notifyItemInserted(MAX_SIZE - 1)
            }
        }

        fun bind(imageUri: Uri) {
            Glide.with(binding.image)
                .load(imageUri)
                .thumbnail(0.5f)
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

    companion object {
        const val MAX_SIZE = 5
    }
}
