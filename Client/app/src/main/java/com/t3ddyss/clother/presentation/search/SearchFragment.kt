package com.t3ddyss.clother.presentation.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.t3ddyss.clother.databinding.FragmentSearchBinding
import com.t3ddyss.clother.util.toEditable
import com.t3ddyss.core.presentation.BaseFragment

class SearchFragment
    : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {
    private val args by navArgs<SearchFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.editTextSearch.text = args.query.toEditable()

        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editTextSearch.text.toString().trim()

                if (query.isNotBlank() && query.isNotEmpty()) {
                    val action = SearchFragmentDirections
                        .searchFragmentToSearchResultsGraph(query)
                    findNavController().navigate(action)
                }

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onStart() {
        super.onStart()

        binding.editTextSearch.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(binding.editTextSearch, InputMethodManager.SHOW_IMPLICIT)
    }
}