package com.t3ddyss.clother.presentation.offer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemOfferBinding
import com.t3ddyss.clother.domain.models.Offer

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
        getItem(position)?.let { holder.bind(it) }
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

        fun bind(offer: Offer) {
            binding.apply {
                Glide.with(image.context)
                    .load(offer.images.firstOrNull())
                    .thumbnail(0.5f)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_offer_image)
                    .dontAnimate()
                    .into(image)

                textViewTitle.text = offer.title
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