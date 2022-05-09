package com.t3ddyss.clother.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentProfileBinding
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.presentation.offers.OfferViewModel
import com.t3ddyss.clother.presentation.offers.OffersAdapter
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.presentation.GridItemDecoration
import com.t3ddyss.core.util.dp
import com.t3ddyss.core.util.showSnackbarWithText
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@AndroidEntryPoint
class ProfileFragment
    : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {
    // Using activityViewModels delegate here to save data across different instances of ProfileFragment
    private val profileViewModel by activityViewModels<ProfileViewModel>()
    private val offerViewModel by activityViewModels<OfferViewModel>()

    private val args by navArgs<ProfileFragmentArgs>()

    private val offersAdapter = OffersAdapter(this::onOfferClick)
    private var loadStateListener: ((CombinedLoadStates) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadStateListener = { state: CombinedLoadStates ->
            when (state.refresh) {
                is LoadState.Loading -> {
                    binding.shimmerList.isVisible = offersAdapter.itemCount < 1
                    binding.list.isVisible = offersAdapter.itemCount > 0
                }

                is LoadState.NotLoading -> {
                    binding.shimmerList.isVisible = false
                    binding.list.isVisible = true

                    binding.emptyState.isVisible = state.append.endOfPaginationReached
                        && offersAdapter.itemCount < 1
                }

                is LoadState.Error -> {
                    val error = (state.refresh as LoadState.Error).error

                    binding.shimmerList.isVisible = false
                    binding.list.isVisible = true
                    showSnackbarWithText(error)
                }
            }
        }.also {
            offersAdapter.addLoadStateListener(it)
        }

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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.buttonEdit.isVisible = args.isCurrentUser
    }

    override fun onDestroyView() {
        loadStateListener?.let {
            offersAdapter.removeLoadStateListener(it)
        }
        loadStateListener = null
        super.onDestroyView()
    }

    private fun subscribeUi() {
        profileViewModel.offers.observe(viewLifecycleOwner) {
            offersAdapter.submitData(lifecycle, it)
        }

        profileViewModel.user.observe(viewLifecycleOwner) {
            binding.collapsingToolbarLayout.title = it.name

            if (it.details != null) {
                binding.textViewSignUpDate.text = it.details.createdAt.toDescription()
                binding.textViewEmail.text = getString(R.string.profile_email, it.details.email)
                binding.shimmerHeader.isVisible = false
                binding.signUpDateContainer.isVisible = true
                binding.emailContainer.isVisible = true

                it.details.age?.let { age ->
                    binding.textViewAge.text = getString(R.string.profile_age, age.toString())
                    binding.ageContainer.isVisible = true
                } ?: run {
                    binding.ageContainer.isVisible = false
                }
            } else {
                binding.signUpDateContainer.isVisible = false
                binding.emailContainer.isVisible = false
                binding.ageContainer.isVisible = false
                binding.shimmerHeader.isVisible = true
            }
        }

        offerViewModel.removedOffers.observe(viewLifecycleOwner) {
            profileViewModel.removeOffers(it)
        }
    }

    private fun onOfferClick(offer: Offer) {
        offerViewModel.selectOffer(offer)
        val action = ProfileFragmentDirections
            .actionProfileFragmentToOfferFragment(offer.user.id)
        findNavController().navigate(action)
    }

    private fun Date.toDescription(): String {
        val diff = System.currentTimeMillis() - this.time
        val descriptionRes = when {
            diff < DAY_MILLIS -> R.string.profile_join_date_today
            diff < WEEK_MILLIS -> R.string.profile_join_date_week
            diff < MONTH_MILLIS -> R.string.profile_join_date_month
            diff < YEAR_MILLIS -> R.string.profile_join_date_year
            else -> R.string.profile_join_date_several_years
        }
        return getString(R.string.profile_join_date, getString(descriptionRes))
    }

    private companion object {
        val DAY_MILLIS = TimeUnit.DAYS.toMillis(1)
        val WEEK_MILLIS = TimeUnit.DAYS.toMillis(7)
        val MONTH_MILLIS = TimeUnit.DAYS.toMillis(30)
        val YEAR_MILLIS = TimeUnit.DAYS.toMillis(365)
    }
}