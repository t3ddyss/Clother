package com.t3ddyss.clother.ui.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentFiltersDialogBinding
import com.t3ddyss.clother.ui.search_results.SearchResultsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class FilterDialogFragment : BottomSheetDialogFragment() {
    private val viewModel by hiltNavGraphViewModels<SearchResultsViewModel>(
            R.id.search_results_graph)
    private var _binding: FragmentFiltersDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentFiltersDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}