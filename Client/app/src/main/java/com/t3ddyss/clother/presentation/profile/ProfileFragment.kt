package com.t3ddyss.clother.presentation.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentProfileBinding
import com.t3ddyss.clother.presentation.offer.OfferViewModel
import com.t3ddyss.clother.presentation.offer.OffersAdapter
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.showSnackbarWithText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment
    : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {
    // Using activityViewModels delegate here to save data across different instances of ProfileFragment
    private val profileViewModel by activityViewModels<ProfileViewModel>()
    private val offerViewModel by activityViewModels<OfferViewModel>()

    private val adapter = OffersAdapter {
        offerViewModel.selectOffer(it)
        val action = ProfileFragmentDirections
            .actionProfileFragmentToOfferFragment(it.userId)
        findNavController().navigate(action)
    }
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadStateListener = {
            when (it.refresh) {
                is LoadState.Loading -> {
                    binding.shimmer.isVisible = adapter.itemCount < 1
                    binding.list.isVisible = adapter.itemCount > 0
                }

                is LoadState.NotLoading -> {
                    binding.shimmer.isVisible = false
                    binding.list.isVisible = true

                    if (it.append.endOfPaginationReached && adapter.itemCount < 1) {
                        binding.emptyState.isVisible = true
                    }
                }

                is LoadState.Error -> {
                    val error = (it.refresh as LoadState.Error).error

                    binding.shimmer.isVisible = false
                    binding.list.isVisible = true
                    showSnackbarWithText(error)
                }
            }
        }
        adapter.addLoadStateListener(loadStateListener)

        val layoutManager = GridLayoutManager(context, 2)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.apply {
            horizontalDecorator.setDrawable(this)
            binding.list.addItemDecoration(horizontalDecorator)
        }

        subscribeUi()
    }

    override fun onDestroyView() {
        adapter.removeLoadStateListener(loadStateListener)
        super.onDestroyView()
    }

    private fun subscribeUi() {
        profileViewModel.offers.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                adapter.submitData(it)
            }
        }

        offerViewModel.removedOffers.observe(viewLifecycleOwner) {
            profileViewModel.removeOffers(it)
        }
    }
}