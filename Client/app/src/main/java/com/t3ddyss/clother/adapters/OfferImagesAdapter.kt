package com.t3ddyss.clother.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemImageBinding
import com.t3ddyss.clother.utilities.getImageUrlForCurrentDevice

class OfferImagesAdapter(
    private val images: List<String>,
    private val clickListener: (Int) -> Unit
) : RecyclerView.Adapter<OfferImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ListItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(
        val binding: ListItemImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                clickListener.invoke(absoluteAdapterPosition)
            }
        }

        fun bind(url: String) {
            binding.apply {
                Glide.with(image)
                    .load(url.getImageUrlForCurrentDevice())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_offer_image)
                    .dontAnimate()
                    .into(image)
            }
        }
    }
}