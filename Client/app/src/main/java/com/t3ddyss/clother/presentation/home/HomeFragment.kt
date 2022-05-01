package com.t3ddyss.clother.presentation.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentHomeBinding
import com.t3ddyss.clother.presentation.offers.OfferViewModel
import com.t3ddyss.clother.presentation.offers.OffersAdapter
import com.t3ddyss.clother.util.getThemeColor
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.presentation.GridItemDecoration
import com.t3ddyss.core.util.dp
import com.t3ddyss.core.util.showSnackbarWithText
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    // Using activityViewModels delegate here to save data across different instances of HomeFragment
    private val viewModel by activityViewModels<HomeViewModel>()
    private val offerViewModel by activityViewModels<OfferViewModel>()

    private val args by navArgs<HomeFragmentArgs>()

    private val adapter = OffersAdapter { offer ->
        offerViewModel.selectOffer(offer)
        val action = HomeFragmentDirections
            .actionHomeFragmentToOfferFragment(offer.userId)
        findNavController().navigate(action)
    }
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit
    private lateinit var adapterDataObserver: RecyclerView.AdapterDataObserver
    private lateinit var onScrollListener: RecyclerView.OnScrollListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.progressBarFooter.isVisible = false
        val layoutManager = GridLayoutManager(context, 2)

        // TODO remove duplicated loadStateListener code if possible
        loadStateListener = {
            val isRefreshInitiatedByUser = binding.swipeRefresh.isRefreshing

            when (it.refresh) {
                is LoadState.Loading -> {
                    binding.shimmer.isVisible = true && !isRefreshInitiatedByUser
                    binding.containerHome.isVisible = false || isRefreshInitiatedByUser
                }

                is LoadState.NotLoading -> {
                    binding.shimmer.isVisible = false
                    binding.swipeRefresh.isRefreshing = false

                    if (it.append.endOfPaginationReached && adapter.itemCount < 1) {
                        binding.emptyState.isVisible = true
                    } else {
                        binding.containerHome.isVisible = true
                    }
                }

                is LoadState.Error -> {
                    val error = (it.refresh as LoadState.Error).error

                    binding.shimmer.isVisible = false
                    binding.containerHome.isVisible = true
                    binding.swipeRefresh.isRefreshing = false
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
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    binding.list.scrollToPosition(positionStart)
                }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)

        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter
        binding.list.addItemDecoration(GridItemDecoration(2, 8.dp().roundToInt(), false))

        // Show progressbar if reached end of current list
        onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    return
                }

                binding.progressBarFooter.isVisible =
                    (!recyclerView.canScrollVertically(1)
                            && newState == RecyclerView.SCROLL_STATE_IDLE
                            && !viewModel.endOfPaginationReachedBottom
                            && (recyclerView.adapter?.itemCount ?: 0) > 0)
            }
        }
        binding.list.addOnScrollListener(onScrollListener)

        context?.getThemeColor(R.attr.colorPrimaryVariant)?.let {
            binding.swipeRefresh.setProgressBackgroundColorSchemeColor(it)
        }
        context?.getThemeColor(R.attr.colorSecondary)?.let {
            binding.swipeRefresh.setColorSchemeColors(it)
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        if (args.createdOfferId != 0) {
            viewModel.setNewOfferAdded(args.createdOfferId)
        }

        subscribeUi()
    }

    override fun onResume() {
        super.onResume()
        binding.swipeRefresh.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        binding.swipeRefresh.isEnabled = false
    }

    override fun onDestroyView() {
        binding.list.removeOnScrollListener(onScrollListener)
        adapter.removeLoadStateListener(loadStateListener)
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
        super.onDestroyView()
    }

    private fun subscribeUi() {
        viewModel.offers.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }

        viewModel.newOfferAdded.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled() ?: return@observe
            showSnackbarWithText(R.string.offer_created)
        }
    }
}