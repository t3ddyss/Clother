package com.t3ddyss.clother.ui.search_results

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentSearchResultsBinding
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class SearchResultsFragment : Fragment() {

    private val viewModel by hiltNavGraphViewModels<SearchResultsViewModel>(
            R.navigation.search_results_graph)
    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<SearchResultsFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)

        Log.d(DEBUG_TAG, (args.category == null).toString())
        Log.d(DEBUG_TAG, (args.query == null).toString())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}