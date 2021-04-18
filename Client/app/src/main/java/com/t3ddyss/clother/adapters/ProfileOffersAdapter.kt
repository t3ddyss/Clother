package com.t3ddyss.clother.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemOfferBinding
import com.t3ddyss.clother.databinding.ListItemProfileBinding
import com.t3ddyss.clother.models.offers.Offer
import com.t3ddyss.clother.models.user.User
import com.t3ddyss.clother.ui.profile.UiModel
import com.t3ddyss.clother.utilities.getImageUrlForCurrentDevice
import java.lang.IllegalArgumentException

class ProfileOffersAdapter(
        private val user: LiveData<User>,
        private val lifecycleOwner: LifecycleOwner,
        private val clickListener: (Offer) -> Unit,
) : PagingDataAdapter<UiModel, RecyclerView.ViewHolder>(UiModelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.list_item_offer) {
            OfferViewHolder(ListItemOfferBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
            ))
        }
        else {
            ProfileViewHolder(
                    binding = ListItemProfileBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                    ),
                    user = user,
                    lifecycleOwner = lifecycleOwner
            ).also {
                val layoutParams = it.itemView.layoutParams
                        as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OfferViewHolder) {
            holder.bind((getItem(position) as UiModel.OfferItem).offer)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UiModel.OfferItem -> R.layout.list_item_offer
            is UiModel.HeaderItem -> R.layout.list_item_profile
            else -> throw IllegalArgumentException()
        }
    }

    inner class OfferViewHolder(
            private val binding: ListItemOfferBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val item = getItem(absoluteAdapterPosition) as UiModel.OfferItem
                clickListener.invoke(item.offer)
            }
        }

        fun bind(offer: Offer) {
            binding.apply {
                Glide.with(image.context)
                        .load(offer.images.firstOrNull()?.getImageUrlForCurrentDevice())
                        .thumbnail(0.5f)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_offer_image)
                        .dontAnimate()
                        .into(image)

                textViewTitle.text = offer.title
            }
        }
    }

    inner class ProfileViewHolder(
            private val binding: ListItemProfileBinding?,
            user: LiveData<User>,
            lifecycleOwner: LifecycleOwner
    ) : RecyclerView.ViewHolder(binding!!.root) {
        init {
            user.observe(lifecycleOwner) {
                if (binding == null) return@observe

                binding.textViewName.text = it.name
                binding.textViewEmail.text = it.email

                if (it.image != null) {
                    Glide.with(binding.cardViewAvatar.imageViewAvatar)
                            .load(it.image.getImageUrlForCurrentDevice())
                            .thumbnail(0.25f)
                            .centerCrop()
                            .into(binding.cardViewAvatar.imageViewAvatar)
                }
            }
        }
    }
}