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
import com.t3ddyss.clother.data.common.common.Mappers.toArg
import com.t3ddyss.clother.databinding.FragmentOfferCategoryBinding
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfferCategoryFragment : BaseFragment<FragmentOfferCategoryBinding>
    (FragmentOfferCategoryBinding::inflate) {

    private val viewModel by viewModels<OfferCategoryViewModel>()
    private val args by navArgs<OfferCategoryFragmentArgs>()
    private lateinit var adapter: CategoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val isRootCategory = args.parentId == 0
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.offer_select_category),
            if (isRootCategory) ToolbarUtils.NavIcon.NONE else ToolbarUtils.NavIcon.UP
        )
        adapter = CategoriesAdapter(this::onCategoryClick)
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

    private fun onCategoryClick(category: Category) {
        val action =  if (category.isLastLevel) {
            OfferCategoryFragmentDirections
                .offerCategoryToOfferEditorGraph(category.toArg())
        } else {
            OfferCategoryFragmentDirections
                .openSubcategoriesAction(category.id)
        }
        findNavController().navigate(action)
    }
}