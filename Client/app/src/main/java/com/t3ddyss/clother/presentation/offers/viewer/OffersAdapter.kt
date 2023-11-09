package com.t3ddyss.clother.presentation.offers.viewer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemOfferBinding
import com.t3ddyss.clother.domain.offers.models.Offer

class OffersAdapter(
    private val clickListener: (Offer) -> Unit
) : PagingDataAdapter<Offer, OffersAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        return OfferViewHolder(
            ListItemOfferBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(
        private val binding: ListItemOfferBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val offer = getItem(absoluteAdapterPosition)

                if (offer != null) {
                    clickListener.invoke(offer)
                }
            }
        }

        fun bind(offer: Offer?) {
            val isPlaceholder = offer == null

            binding.apply {
                image.isInvisible = isPlaceholder
                title.isInvisible = isPlaceholder
                progressBar.isVisible = isPlaceholder

                if (offer != null) {
                    Glide.with(image.context)
                        .load(offer.images.firstOrNull())
                        .thumbnail(0.5f)
                        .centerCrop()
                        .placeholder(R.drawable.image_placeholder)
                        .dontAnimate()
                        .into(image)

                    title.text = offer.title
                }
            }
        }
    }

    private class OfferDiffCallback : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(oldItem: Offer, newItem: Offer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Offer, newItem: Offer): Boolean {
            return oldItem == newItem
        }
    }
}