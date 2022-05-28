package com.t3ddyss.clother.presentation.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.common.common.Mappers.toArg
import com.t3ddyss.clother.databinding.FragmentSearchResultsBinding
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.presentation.offers.OfferViewModel
import com.t3ddyss.clother.presentation.offers.OffersAdapter
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.presentation.GridItemDecoration
import com.t3ddyss.core.util.extensions.dp
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.ToolbarUtils
import com.t3ddyss.feature_location.presentation.LocationSelectorFragment
import com.t3ddyss.navigation.util.observeNavigationResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchResultsFragment
    : BaseFragment<FragmentSearchResultsBinding>(FragmentSearchResultsBinding::inflate) {

    private val viewModel by hiltNavGraphViewModels<SearchResultsViewModel>(
        R.id.search_results_graph
    )
    private val filtersViewModel by hiltNavGraphViewModels<FiltersViewModel>(
        R.id.search_results_graph
    )
    private val offerViewModel by activityViewModels<OfferViewModel>()
    private val args by navArgs<SearchResultsFragmentArgs>()

    private val adapter = OffersAdapter(this::onOfferClick)
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit
    private lateinit var onScrollListener: RecyclerView.OnScrollListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            "",
            ToolbarUtils.NavIcon.UP
        )
        setHasOptionsMenu(true)
    }

    // Using onStart() because of navGraphViewModels
    override fun onStart() {
        super.onStart()

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
        binding.list.addItemDecoration(GridItemDecoration(2, 8.dp(), true))

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
        return when (item.itemId) {
            R.id.filters -> {
                val navController = findNavController()
                if (navController.currentBackStackEntry?.destination?.id
                    != R.id.filterDialogFragment
                ) {
                    navController.navigate(R.id.action_searchResultsFragment_to_filterDialogFragment)
                }
                true
            }
            R.id.search -> {
                val action = SearchResultsFragmentDirections
                    .actionSearchResultsToSearchFragment(args.query.orEmpty())
                findNavController().navigate(action)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroyView() {
        adapter.removeLoadStateListener(loadStateListener)
        binding.list.removeOnScrollListener(onScrollListener)
        super.onDestroyView()
    }

    private fun subscribeUi() {
        viewModel.offers.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }

        observeNavigationResult<String>(LocationSelectorFragment.COORDINATES_KEY) {
            filtersViewModel.onLocationSelected(it)
        }
    }

    private fun onOfferClick(offer: Offer) {
        offerViewModel.selectOffer(offer)
        val action = SearchResultsFragmentDirections
            .actionSearchResultsToOfferFragment(offer.user.toArg())
        findNavController().navigate(action)
    }
}