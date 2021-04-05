package com.t3ddyss.clother.ui.offer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.tabs.TabLayoutMediator
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OfferImagesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferBinding
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.getImageUrlForCurrentDevice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OfferFragment : Fragment() {

    private val viewModel by viewModels<OfferViewModel>()
    private var _binding: FragmentOfferBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<OfferFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentOfferBinding.inflate(inflater, container, false)

        val offerId = args.offerId

        viewModel.offer.observe(viewLifecycleOwner) {
            with (binding) {
                images.adapter = OfferImagesAdapter(it.images) {num->
                    Log.d(DEBUG_TAG, it.images[num].getImageUrlForCurrentDevice())
                }

                TabLayoutMediator(dots, images) {_, _ -> }.attach()
            }
        }
        viewModel.getOffer(offerId)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}