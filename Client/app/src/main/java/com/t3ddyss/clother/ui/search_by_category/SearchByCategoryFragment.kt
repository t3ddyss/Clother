package com.t3ddyss.clother.ui.search_by_category

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.CategoriesAdapter
import com.t3ddyss.clother.databinding.FragmentSearchByCategoryBinding
import com.t3ddyss.clother.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchByCategoryFragment
    : BaseFragment<FragmentSearchByCategoryBinding>(FragmentSearchByCategoryBinding::inflate) {

    private val viewModel by viewModels<SearchByCategoryViewModel>()
    private val args by navArgs<SearchByCategoryFragmentArgs>()

    private lateinit var adapter: CategoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parentId = args.parentId.let { if (it == 0) null else it }
        setHasOptionsMenu(parentId == null)

        val layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )

        adapter = CategoriesAdapter {
            if (!it.isLastLevel) {
                val action = SearchByCategoryFragmentDirections
                    .openSubcategoriesAction(it.id)
                findNavController().navigate(action)
            } else {
                val action = SearchByCategoryFragmentDirections
                    .searchByCategoryToSearchResultsGraph(it)
                findNavController().navigate(action)
            }
        }

        binding.listCategories.layoutManager = layoutManager
        binding.listCategories.adapter = adapter

        val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider_small)?.apply {
            verticalDecorator.setDrawable(this)

            binding.listCategories.addItemDecoration(verticalDecorator)
        }

        subscribeUi()
        viewModel.getCategories(parentId)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_search_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        findNavController().navigate(R.id.action_searchByCategoryFragment_to_searchFragment)
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeUi() {
        viewModel.categories.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}