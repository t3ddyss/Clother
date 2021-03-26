package com.t3ddyss.clother.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OffersAdapter
import com.t3ddyss.clother.databinding.FragmentHomeBinding
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import com.t3ddyss.clother.utilities.getThemeColor
import com.t3ddyss.clother.viewmodels.NetworkStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalPagingApi
class HomeFragment : Fragment() {

    private val homeViewModel by viewModels<HomeViewModel>()
    private val networkStateViewModel by activityViewModels<NetworkStateViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var prefs: SharedPreferences

    private val adapter = OffersAdapter()
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit

    private var offersJob: Job? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        loadStateListener = {
            val isRefreshInitiatedByUser = binding.swipeRefreshHome.isRefreshing

            when (it.refresh) {
                is LoadState.Loading -> {
                    binding.shimmerHome.isVisible = true && !isRefreshInitiatedByUser
                    binding.containerHome.isVisible = false || isRefreshInitiatedByUser
                }

                is LoadState.NotLoading -> {
                    binding.shimmerHome.isVisible = false
                    binding.containerHome.isVisible = true
                    binding.swipeRefreshHome.isRefreshing = false
                }

                is LoadState.Error -> {
                    val error = (it.refresh as LoadState.Error).error

                    if (error is HttpException && error.code() == 401) {
                        findNavController().navigate(R.id.action_homeFragment_to_signUpFragment)

                        (activity as? MainActivity)
                            ?.showGenericError(getString(R.string.session_expired))
                        prefs.edit().remove(IS_AUTHENTICATED).apply()
                    }
                    else {
                        binding.shimmerHome.isVisible = false
                        binding.containerHome.isVisible = true
                        binding.swipeRefreshHome.isRefreshing = false

                        (activity as? MainActivity)
                            ?.showGenericError(error)
                    }
                }
            }

            // Hide footer with progress bar
            if (it.append !is LoadState.Loading) {
                binding.progressBarFooter.isVisible = false
                homeViewModel.endOfPaginationReachedBottom = it.append.endOfPaginationReached

                // Disable bottom padding when end of pagination is reached
                if (it.append.endOfPaginationReached) {
                    binding.recyclerViewHomeOffers.setPadding(0, 0, 0, 0)
                }
            }
        }
        adapter.addLoadStateListener(loadStateListener)
        binding.recyclerViewHomeOffers.adapter = adapter

        // Show progressbar if reached end of current list
        binding.recyclerViewHomeOffers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                binding.progressBarFooter.isVisible =
                        (!recyclerView.canScrollVertically(1)
                        && newState==RecyclerView.SCROLL_STATE_IDLE
                        && !homeViewModel.endOfPaginationReachedBottom)
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
        }

        networkStateViewModel.isNetworkAvailable.observe(viewLifecycleOwner, {
            if (it.first) {
                if (it.second) {
                    adapter.retry()
                } else {
                    (activity as? MainActivity)?.showConnectionError()
                }
            }
        })

        getOffers()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.removeLoadStateListener(loadStateListener)
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