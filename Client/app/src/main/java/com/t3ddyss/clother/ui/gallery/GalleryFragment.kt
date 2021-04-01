package com.t3ddyss.clother.ui.gallery

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.GalleryImagesAdapter
import com.t3ddyss.clother.databinding.FragmentGalleryBinding
import com.t3ddyss.clother.models.GalleryImage
import com.t3ddyss.clother.ui.offer_editor.OfferEditorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class GalleryFragment : Fragment() {
    private val viewModel by viewModels<GalleryViewModel>()
    private val editorViewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GalleryImagesAdapter
    private lateinit var layoutManager: GridLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        layoutManager = GridLayoutManager(context, 3)
        adapter = GalleryImagesAdapter {
            (activity as? MainActivity)?.showGenericError(getString(R.string.attach_limit_exceeded))
        }

        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter
        binding.list.setItemViewCacheSize(100)

        viewModel.images.observe(viewLifecycleOwner) { images ->
            val previouslySelectedImages = editorViewModel.images.value!!

            images.filter {
                 it.uri in previouslySelectedImages
            }.forEach {
                it.isSelected = true
            }

            adapter.submitList(images)
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                layoutManager.scrollToPositionWithOffset(positionStart, 0)
            }
        })

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.apply {
            verticalDecorator.setDrawable(this)
            horizontalDecorator.setDrawable(this)

            binding.list.addItemDecoration(horizontalDecorator)
            binding.list.addItemDecoration(verticalDecorator)
        }

        viewModel.getImages()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_apply_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.apply) {
            val selectedImages = adapter.currentList.filter {
                it.isSelected
            }.map {
                it.uri
            }.toMutableList()

            if (selectedImages.isNotEmpty()) {
                editorViewModel.images.value = selectedImages
                findNavController().popBackStack()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}