package com.t3ddyss.clother.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OffersAdapter
import com.t3ddyss.clother.databinding.FragmentHomeBinding
import com.t3ddyss.clother.utilities.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
@ExperimentalPagingApi
class HomeFragment : Fragment() {

    private val homeViewModel by viewModels<HomeViewModel>()

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
        binding.recyclerViewHomeOffers.adapter = adapter

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