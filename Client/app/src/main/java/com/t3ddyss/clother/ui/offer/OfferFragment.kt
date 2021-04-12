package com.t3ddyss.clother.ui.offer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.tabs.TabLayoutMediator
import com.t3ddyss.clother.adapters.OfferImagesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferBinding
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OfferFragment : Fragment() {

    private val viewModel by activityViewModels<OfferViewModel>()
    private var _binding: FragmentOfferBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<OfferFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentOfferBinding.inflate(inflater, container, false)

        viewModel.offer.observe(viewLifecycleOwner) {
            with (binding) {
                images.adapter = OfferImagesAdapter(it.images) {
                }
                if (it.images.size > 1) {
                    TabLayoutMediator(dots, images) { _, _ ->
                    }.attach()
                }

                Log.d(DEBUG_TAG, it.images[0])

                textViewTitle.text = it.title

                if (!it.description.isNullOrEmpty()) {
                    textViewDescription.text = it.description
                }
                else {
                    textViewDescription.isVisible = false
                }

                if (it.category.isNotEmpty()) {
                    textViewCategory.text = it.category
                }
                else {
                    groupCategory.isVisible = false
                }

                if (!it.location.isNullOrEmpty()) {
                    location.setOnClickListener { _ ->
                        val action = OfferFragmentDirections
                            .actionOfferFragmentToLocationViewerFragment(it.location!!)
                        findNavController().navigate(action)
                    }
                }
                else {
                    groupLocation.isVisible = false
                }

                if (!it.size.isNullOrEmpty()) {
                    textViewSize.text = it.size
                }
                else {
                    groupSize.isVisible = false
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}