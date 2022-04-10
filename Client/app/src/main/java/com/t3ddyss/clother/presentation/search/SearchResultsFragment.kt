package com.t3ddyss.clother.presentation.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentSearchResultsBinding
import com.t3ddyss.clother.presentation.offer.OfferViewModel
import com.t3ddyss.clother.presentation.offer.OffersAdapter
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.showSnackbarWithText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchResultsFragment
    : BaseFragment<FragmentSearchResultsBinding>(FragmentSearchResultsBinding::inflate) {

    private val viewModel by hiltNavGraphViewModels<SearchResultsViewModel>(
        R.id.search_results_graph
    )
    private val offerViewModel by activityViewModels<OfferViewModel>()
    private val args by navArgs<SearchResultsFragmentArgs>()

    private val adapter = OffersAdapter { offer ->
        offerViewModel.selectOffer(offer)
        val action = SearchResultsFragmentDirections
            .actionSearchResultsToOfferFragment(offer.userId)
        findNavController().navigate(action)
    }
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit
    private lateinit var onScrollListener: RecyclerView.OnScrollListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        loadStateListener = {
            when (it.refresh) {
                is LoadState.Loading -> {
                    binding.shimmer.isVisible = true
                    binding.containerSearch.isVisible = false
                    binding.emptyState.isVisible = false
                }

                is LoadState.NotLoading -> {
                    binding.shimmer.isVisible = false

                    if (it.append.endOfPaginationReached && adapter.itemCount < 1) {
                        binding.emptyState.isVisible = true
                    } else {
                        binding.containerSearch.isVisible = true
                    }
                }

                is LoadState.Error -> {
                    val error = (it.refresh as LoadState.Error).error

                    binding.shimmer.isVisible = false
                    binding.containerSearch.isVisible = true
                    showSnackbarWithText(error)
                }
            }

            // Hide footer with progress bar
            if (it.append !is LoadState.Loading) {
                binding.progressBarFooter.isVisible = false
                viewModel.endOfPaginationReachedBottom = it.append.endOfPaginationReached

                // Disable bottom padding when end of pagination is reached
                if (it.append.endOfPaginationReached) {
                    binding.list.setPadding(0, 0, 0, 0)
                }
            }
        }
        adapter.addLoadStateListener(loadStateListener)

        val layoutManager = GridLayoutManager(context, 2)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.apply {
            verticalDecorator.setDrawable(this)
            horizontalDecorator.setDrawable(this)

            binding.list.addItemDecoration(horizontalDecorator)
            binding.list.addItemDecoration(verticalDecorator)
        }

        // Show progressbar if reached end of current list
        onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                binding.progressBarFooter.isVisible =
                    (!recyclerView.canScrollVertically(1)
                            && newState == RecyclerView.SCROLL_STATE_IDLE
                            && !viewModel.endOfPaginationReachedBottom
                            && (recyclerView.adapter?.itemCount ?: 0) > 0)
            }
        }
        binding.list.addOnScrollListener(onScrollListener)

        subscribeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_search_results_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filters -> {
                val navController = findNavController()
                if (navController.currentBackStackEntry?.destination?.id
                    != R.id.filterDialogFragment
                ) {
                    navController.navigate(R.id.action_searchResultsFragment_to_filterDialogFragment)
                }
            }

            R.id.search -> {
                val action = SearchResultsFragmentDirections.actionSearchResultsToSearchFragment(
                    args.query ?: ""
                )
                findNavController().navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        adapter.removeLoadStateListener(loadStateListener)
        binding.list.removeOnScrollListener(onScrollListener)
        super.onDestroyView()
    }

    private fun subscribeUi() {
        viewModel.offers.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                adapter.submitData(it)
            }
        }

        viewModel.filters.observe(viewLifecycleOwner) {
            val query = getQuery()
            viewModel.getOffers(query)
        }
    }

    private fun getQuery(): Map<String, String> {
        val query = mutableMapOf<String, String>()

        args.category?.let {
            query["category"] = it.id.toString()
        }

        args.query?.let {
            query["query"] = it
        }

        viewModel.size.value?.let {
            query["size"] = it
        }

        viewModel.location.value?.let {
            query["location"] = "${it.first.latitude},${it.first.longitude}"
            query["radius"] = it.second.toString()
        }

        return query
    }
}