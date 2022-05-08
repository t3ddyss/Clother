package com.t3ddyss.clother.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.t3ddyss.clother.databinding.FragmentProfileBinding
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.presentation.offers.OfferViewModel
import com.t3ddyss.clother.presentation.offers.OffersAdapter
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.presentation.GridItemDecoration
import com.t3ddyss.core.util.dp
import com.t3ddyss.core.util.showSnackbarWithText
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class ProfileFragment
    : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {
    // Using activityViewModels delegate here to save data across different instances of ProfileFragment
    private val profileViewModel by activityViewModels<ProfileViewModel>()
    private val offerViewModel by activityViewModels<OfferViewModel>()

    private val offersAdapter = OffersAdapter(this::onOfferClick)
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadStateListener = {
            when (it.refresh) {
                is LoadState.Loading -> {
                    binding.shimmer.isVisible = offersAdapter.itemCount < 1
                    binding.list.isVisible = offersAdapter.itemCount > 0
                }

                is LoadState.NotLoading -> {
                    binding.shimmer.isVisible = false
                    binding.list.isVisible = true

                    binding.emptyState.isVisible = it.append.endOfPaginationReached
                        && offersAdapter.itemCount < 1
                }

                is LoadState.Error -> {
                    val error = (it.refresh as LoadState.Error).error

                    binding.shimmer.isVisible = false
                    binding.list.isVisible = true
                    showSnackbarWithText(error)
                }
            }
        }
        offersAdapter.addLoadStateListener(loadStateListener)

        val layoutManager = GridLayoutManager(context, 2)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = offersAdapter
        binding.list.addItemDecoration(GridItemDecoration(2, 8.dp().roundToInt(), true))
//
//        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
//            if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
//                binding.collapsingToolbarLayout.title = "aaaaa"
//            } else {
//                binding.collapsingToolbarLayout.title = "bbbbb"
//            }
//        })

        subscribeUi()
    }

    override fun onDestroyView() {
        offersAdapter.removeLoadStateListener(loadStateListener)
        super.onDestroyView()
    }

    private fun subscribeUi() {
        profileViewModel.offers.observe(viewLifecycleOwner) {
            offersAdapter.submitData(lifecycle, it)
        }

        profileViewModel.user.observe(viewLifecycleOwner) {
            binding.collapsingToolbarLayout.title = it.name
        }

        offerViewModel.removedOffers.observe(viewLifecycleOwner) {
            profileViewModel.removeOffers(it)
        }
    }

    private fun onOfferClick(offer: Offer) {
        offerViewModel.selectOffer(offer)
        val action = ProfileFragmentDirections
            .actionProfileFragmentToOfferFragment(offer.userId)
        findNavController().navigate(action)
    }
}