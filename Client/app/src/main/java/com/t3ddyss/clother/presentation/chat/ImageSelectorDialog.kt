package com.t3ddyss.clother.presentation.chat

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.t3ddyss.clother.databinding.DialogImageSelectorBinding
import com.t3ddyss.core.presentation.GridItemDecoration
import com.t3ddyss.core.util.extensions.dp
import com.t3ddyss.navigation.util.setNavigationResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageSelectorDialog : BottomSheetDialogFragment() {
    private val viewModel by viewModels<ImageSelectorViewModel>()

    private var _binding: DialogImageSelectorBinding? = null
    private val binding get() = _binding!!

    private var adapter: ImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImageSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val layoutManager = GridLayoutManager(context, 3)
        adapter = ImagesAdapter(this::onImageClick)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter
        binding.list.setItemViewCacheSize(100)
        binding.list.addItemDecoration(GridItemDecoration(3, 8.dp(), true))

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.images.observe(viewLifecycleOwner) {
            adapter?.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onImageClick(image: Uri) {
        setNavigationResult(SELECTED_IMAGE, image.toString())
        findNavController().popBackStack()
    }

    companion object {
        const val SELECTED_IMAGE = "selected_image"
    }
}