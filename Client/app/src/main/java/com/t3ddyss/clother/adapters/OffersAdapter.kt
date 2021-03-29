package com.t3ddyss.clother.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemOfferBinding
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.utilities.DEBUG_TAG

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
                val url = GlideUrl(offer.image) { mapOf(Pair("User-Agent", "Mozilla/5.0 " +
                        "(Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/89.0.4389.90 Safari/537.36")) }
                Log.d(DEBUG_TAG, url.toStringUrl())

                binding.apply {
                    Glide.with(image.context)
                            .load(offer.image)
                            .centerCrop()
                            .placeholder(R.drawable.placeholder_offer_image)
                            .dontAnimate()
                            .into(image)

                    textViewTitle.text = offer.title
                }
            }
    }
}