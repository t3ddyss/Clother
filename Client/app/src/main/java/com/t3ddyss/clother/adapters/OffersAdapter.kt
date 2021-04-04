package com.t3ddyss.clother.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemOfferBinding
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.utilities.getImageUrlForCurrentDevice

class OffersAdapter : PagingDataAdapter<Offer, OffersAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        return OfferViewHolder(ListItemOfferBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class OfferViewHolder(
        private val binding: ListItemOfferBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(offer: Offer) {
                binding.apply {
                    Glide.with(image.context)
                            .load(offer.images.firstOrNull()?.getImageUrlForCurrentDevice())
                            .centerCrop()
                            .placeholder(R.drawable.placeholder_offer_image)
                            .dontAnimate()
                            .into(image)

                    textViewTitle.text = offer.title
                }
            }
    }
}