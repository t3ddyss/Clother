package com.t3ddyss.clother.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.GalleryImagesAdapter
import com.t3ddyss.clother.databinding.FragmentGalleryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryFragment : Fragment() {
    private val viewModel by viewModels<GalleryViewModel>()

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GalleryImagesAdapter
    private lateinit var layoutManager: GridLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        viewModel.images.observe(viewLifecycleOwner) {
            adapter = GalleryImagesAdapter(it)
            layoutManager = GridLayoutManager(context, 3)

            binding.list.layoutManager = layoutManager
            binding.list.adapter = adapter
            binding.list.setHasFixedSize(true)
//            binding.list.setItemViewCacheSize(100)

            val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
            val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

            ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.apply {
                verticalDecorator.setDrawable(this)
                horizontalDecorator.setDrawable(this)

                binding.list.addItemDecoration(horizontalDecorator)
                binding.list.addItemDecoration(verticalDecorator)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}