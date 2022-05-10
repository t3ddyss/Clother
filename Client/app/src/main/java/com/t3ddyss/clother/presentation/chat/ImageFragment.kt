package com.t3ddyss.clother.presentation.chat

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.t3ddyss.clother.databinding.FragmentImageBinding
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.utils.ToolbarUtils

class ImageFragment : BaseFragment<FragmentImageBinding>(FragmentImageBinding::inflate) {
    private val args by navArgs<ImageFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            "",
            ToolbarUtils.NavIcon.UP
        )
        Glide.with(view.context)
            .load(args.url)
            .into(binding.image)
    }
}