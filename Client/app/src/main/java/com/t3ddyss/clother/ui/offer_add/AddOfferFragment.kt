package com.t3ddyss.clother.ui.offer_add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.CategoryAdapter
import com.t3ddyss.clother.databinding.FragmentOfferAddBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalPagingApi
class AddOfferFragment : Fragment() {

    private val addOfferViewModel by viewModels<AddOfferViewModel>()
    private var _binding: FragmentOfferAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferAddBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false)
        binding.listCategories.layoutManager = layoutManager
        addOfferViewModel.categories.observe(viewLifecycleOwner) {
            val adapter = CategoryAdapter(it) {
            }

            binding.listCategories.adapter = adapter
        }

//        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider_small)?.apply {
            verticalDecorator.setDrawable(this)
//            horizontalDecorator.setDrawable(this)

//            binding.listCategories.addItemDecoration(horizontalDecorator)
            binding.listCategories.addItemDecoration(verticalDecorator)
        }

        addOfferViewModel.getCategories(1)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}