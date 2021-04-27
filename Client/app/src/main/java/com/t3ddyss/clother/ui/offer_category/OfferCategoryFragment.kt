package com.t3ddyss.clother.ui.offer_category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.CategoriesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferCategoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfferCategoryFragment : Fragment() {

    private val viewModel by viewModels<OfferCategoryViewModel>()

    private var _binding: FragmentOfferCategoryBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<OfferCategoryFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferCategoryBinding.inflate(inflater, container, false)
        val parentId = args.parentId.let { if (it == 0) null else it }
        (activity as? MainActivity)?.setNavIconVisibility(parentId != null)

        val layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false)

        val adapter = CategoriesAdapter {
            if (!it.isLastLevel) {
                val action = OfferCategoryFragmentDirections
                    .openSubcategoriesAction(it.id)
                findNavController().navigate(action)
            }

            else {
                val action = OfferCategoryFragmentDirections
                    .offerCategoryToOfferEditorGraph(it)
                findNavController().navigate(action)
            }
        }

        binding.listCategories.layoutManager = layoutManager
        binding.listCategories.adapter = adapter

        viewModel.categories.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider_small)?.apply {
            verticalDecorator.setDrawable(this)

            binding.listCategories.addItemDecoration(verticalDecorator)
        }

        viewModel.getCategories(parentId)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}