package com.t3ddyss.clother.presentation.categories

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOfferCategoryBinding
import com.t3ddyss.core.presentation.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfferCategoryFragment : BaseFragment<FragmentOfferCategoryBinding>
    (FragmentOfferCategoryBinding::inflate) {

    private val viewModel by viewModels<OfferCategoryViewModel>()
    private val args by navArgs<OfferCategoryFragmentArgs>()

    private lateinit var adapter: CategoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CategoriesAdapter {
            if (!it.isLastLevel) {
                val action = OfferCategoryFragmentDirections
                    .openSubcategoriesAction(it.id)
                findNavController().navigate(action)
            } else {
                val action = OfferCategoryFragmentDirections
                    .offerCategoryToOfferEditorGraph(it)
                findNavController().navigate(action)
            }
        }

        val layoutManager = LinearLayoutManager(context)

        binding.listCategories.layoutManager = layoutManager
        binding.listCategories.adapter = adapter

        val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider_small)?.apply {
            verticalDecorator.setDrawable(this)

            binding.listCategories.addItemDecoration(verticalDecorator)
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.categories.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}