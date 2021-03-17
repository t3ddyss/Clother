package com.t3ddyss.clother.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OffersAdapter
import com.t3ddyss.clother.databinding.FragmentHomeBinding
import com.t3ddyss.clother.ui.shared_viewmodels.NetworkStateViewModel
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
@ExperimentalPagingApi
class HomeFragment : Fragment() {

    private val homeViewModel by viewModels<HomeViewModel>()
    private val networkStateViewModel by activityViewModels<NetworkStateViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val adapter = OffersAdapter()

    private var offersJob: Job? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        adapter.addLoadStateListener {
            if (it.append !is LoadState.Loading) {
                binding.progressBar.isVisible = false
                homeViewModel.endOfPaginationReachedBottom = it.append.endOfPaginationReached

                if (it.append.endOfPaginationReached) {
                    binding.recyclerViewHomeOffers.setPadding(0, 0, 0, 0)
                }
            }
        }

        binding.recyclerViewHomeOffers.adapter = adapter
        binding.recyclerViewHomeOffers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)
                        && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    if (!homeViewModel.endOfPaginationReachedBottom) {
                        binding.progressBar.isVisible = true
                    }
                }
                else {
                    binding.progressBar.isVisible = false
                }
            }
        })

        context?.getThemeColor(R.attr.colorPrimaryVariant)?.let {
            binding.swipeRefreshHome.setProgressBackgroundColorSchemeColor(it)
        }
        context?.getThemeColor(R.attr.colorSecondary)?.let {
            binding.swipeRefreshHome.setColorSchemeColors(it)
        }

        binding.swipeRefreshHome.setOnRefreshListener {
            adapter.refresh()
            binding.swipeRefreshHome.isRefreshing = false
        }


        networkStateViewModel.isNetworkAvailable.observe(viewLifecycleOwner, {
            if (it.first) {
                if (it.second) {
                    Log.d(DEBUG_TAG, "NETWORK AVAILABLE AGAIN")
//                    adapter.retry()
                }

                else {
                    Log.d(DEBUG_TAG, "NETWORK UNAVAILABLE")
                }
            }
        })

        getOffers()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getOffers(query: Map<String, String> = HashMap()) {
        offersJob?.cancel()

        offersJob = lifecycleScope.launch {
            homeViewModel.getOffers(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }
}