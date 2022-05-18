package com.t3ddyss.clother.presentation.profile

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.t3ddyss.clother.R

object AvatarLoader {

    // TODO make it work
    private val shimmer by lazy {
        Shimmer.AlphaHighlightBuilder()
            .setDuration(300)
            .setBaseAlpha(0.7f)
            .setHighlightAlpha(0.6f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()
    }

    fun loadAvatar(
        imageView: ImageView,
        image: String?,
        @DrawableRes defaultImage: Int = R.drawable.ic_avatar_default
    ) {
        if (!image.isNullOrBlank()) {
            Glide.with(imageView)
                .load(image)
                .placeholder(R.drawable.image_placeholder)
                .centerCrop()
                .into(imageView)
        } else {
            imageView.setImageResource(defaultImage)
        }
    }
}