package com.t3ddyss.clother.presentation.profile

import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.t3ddyss.clother.R
import com.t3ddyss.core.util.extensions.getThemeColor

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
        image: Uri?,
        @DrawableRes defaultImage: Int
    ) {
        if (image != null) {
            Glide.with(imageView)
                .load(image)
                .placeholder(R.drawable.image_placeholder)
                .centerCrop()
                .into(imageView)
        } else {
            val context = imageView.context
            val layerDrawable = context.getDrawable(defaultImage) as LayerDrawable
            val drawable = DrawableCompat.wrap(layerDrawable.findDrawableByLayerId(R.id.icon))
            DrawableCompat.setTint(drawable, context.getThemeColor(R.attr.colorSecondary))
            imageView.setImageDrawable(layerDrawable)
        }
    }
}