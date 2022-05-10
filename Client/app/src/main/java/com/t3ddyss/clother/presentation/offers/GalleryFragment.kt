package com.t3ddyss.clother.presentation.offers

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentGalleryBinding
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.presentation.GridItemDecoration
import com.t3ddyss.core.util.extensions.dp
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryFragment : BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::inflate) {
    private val viewModel by viewModels<GalleryViewModel>()
    private val editorViewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)

    private lateinit var adapter: GalleryImagesAdapter
    private lateinit var adapterDataObserver: RecyclerView.AdapterDataObserver
    private lateinit var layoutManager: GridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.offer_select_images),
            ToolbarUtils.NavIcon.CLOSE
        )
        setHasOptionsMenu(true)

        layoutManager = GridLayoutManager(context, 3)
        adapter = GalleryImagesAdapter(this::onImageLimitExceeded)

        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter
        binding.list.setItemViewCacheSize(100)
        binding.list.addItemDecoration(GridItemDecoration(3, 8.dp(), false))

        adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    binding.list.scrollToPosition(positionStart)
                }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)

        subscribeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_apply_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.apply -> {
                onApplyClick()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroyView() {
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
        super.onDestroyView()
    }

    private fun subscribeUi() {
        viewModel.images.observe(viewLifecycleOwner) { images ->
            val previouslySelectedImages = editorViewModel.images.value!!

            images.filter {
                it.uri in previouslySelectedImages
            }.forEach {
                it.isSelected = true
            }

            adapter.submitList(images)
        }
    }

    private fun onApplyClick() {
        val selectedImages = adapter.currentList
            .filter {
                it.isSelected
            }.map {
                it.uri
            }.toMutableList()

        if (selectedImages.isNotEmpty()) {
            editorViewModel.images.value = selectedImages
            findNavController().popBackStack()
        }
    }

    private fun onImageLimitExceeded() {
        showSnackbarWithText(R.string.offer_attach_limit_exceeded)
    }
}