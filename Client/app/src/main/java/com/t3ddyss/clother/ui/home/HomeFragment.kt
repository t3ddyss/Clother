package com.t3ddyss.clother.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OffersAdapter
import com.t3ddyss.clother.databinding.FragmentHomeBinding
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import com.t3ddyss.clother.utilities.getThemeColor
import com.t3ddyss.clother.viewmodels.NetworkStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class HomeFragment : Fragment() {

    // Using activityViewModels delegate here to save data across different instances of HomeFragment
    private val viewModel by activityViewModels<HomeViewModel>()
    private val networkStateViewModel by activityViewModels<NetworkStateViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var prefs: SharedPreferences

    private val adapter = OffersAdapter {id ->
        val action = HomeFragmentDirections.actionHomeFragmentToOfferFragment(id)
        findNavController().navigate(action)
    }
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val layoutManager = GridLayoutManager(context, 2)

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
                    }
                    else {
                        binding.containerHome.isVisible = true
                    }
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
                        binding.shimmer.isVisible = false
                        binding.containerHome.isVisible = true
                        binding.swipeRefresh.isRefreshing = false

                        (activity as? MainActivity)
                            ?.showGenericError(error)
                    }
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
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                binding.progressBarFooter.isVisible =
                        (!recyclerView.canScrollVertically(1)
                        && newState==RecyclerView.SCROLL_STATE_IDLE
                        && !viewModel.endOfPaginationReachedBottom
                        && (recyclerView.adapter?.itemCount ?: 0) > 0)
            }
        })

        context?.getThemeColor(R.attr.colorPrimaryVariant)?.let {
            binding.swipeRefresh.setProgressBackgroundColorSchemeColor(it)
        }
        context?.getThemeColor(R.attr.colorSecondary)?.let {
            binding.swipeRefresh.setColorSchemeColors(it)
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        networkStateViewModel.isNetworkAvailable.observe(viewLifecycleOwner, {
            if (it.first) {
                if (it.second) {
                    adapter.retry()
                } else {
                    (activity as? MainActivity)?.showGenericError(getString(R.string.no_connection))
                }
            }
        })

        viewModel.offers.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                adapter.submitData(it)
            }
        }
        viewModel.getOffers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            delay(250) // Bad practice, add loading state layout to Login fragment layout itself
            (activity as? MainActivity)?.setLoadingVisibility(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.removeLoadStateListener(loadStateListener)
        _binding = null
    }
}