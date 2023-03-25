package com.t3ddyss.clother.presentation.profile

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.common.common.Mappers.toArg
import com.t3ddyss.clother.data.offers.PagingErrorWrapperException
import com.t3ddyss.clother.databinding.FragmentProfileBinding
import com.t3ddyss.clother.domain.auth.models.UserDetails
import com.t3ddyss.clother.domain.auth.models.UserInfoState
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.presentation.offers.viewer.OffersAdapter
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.presentation.GridItemDecoration
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.extensions.dp
import com.t3ddyss.core.util.extensions.getThemeDimension
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.extensions.textRes
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ProfileFragment
    : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {
    // Using activityViewModels delegate here to save data across different instances of ProfileFragment
    private val profileViewModel by viewModels<ProfileViewModel>()

    private val args by navArgs<ProfileFragmentArgs>()

    private val offersAdapter = OffersAdapter(this::onOfferClick)
    private var loadStateListener: ((CombinedLoadStates) -> Unit)? = null

    private val isCurrentUser get() = args.user == null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbarIfNeeded()
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
                    val error = ((state.refresh as LoadState.Error).error as PagingErrorWrapperException).source

                    binding.shimmerList.isVisible = false
                    binding.list.isVisible = true
                    showSnackbarWithText(error.textRes)
                }
            }
        }.also {
            offersAdapter.addLoadStateListener(it)
        }

        val layoutManager = GridLayoutManager(context, 2)
        binding.list.layoutManager = layoutManager
        binding.list.adapter = offersAdapter
        binding.list.addItemDecoration(GridItemDecoration(2, 8.dp, true))

        subscribeUi()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.buttonEdit.isVisible = isCurrentUser
        binding.buttonMessage.isVisible = !isCurrentUser
        binding.buttonEdit.setOnClickListener {
            findNavController().navigate(
                ProfileFragmentDirections.actionProfileFragmentToProfileEditorFragment()
            )
        }
        binding.buttonMessage.setOnClickListener {
            requireNotNull(args.user)
            findNavController().navigate(
                ProfileFragmentDirections.actionProfileFragmentToChatFragment(
                    args.user!!
                )
            )
        }
    }

    override fun onDestroyView() {
        loadStateListener?.let {
            offersAdapter.removeLoadStateListener(it)
        }
        loadStateListener = null
        super.onDestroyView()
    }

    private fun subscribeUi() {
        profileViewModel.user.collectViewLifecycleAware { info ->
            binding.shimmerHeader.isVisible = info.user.details == null && info is UserInfoState.Cache
            binding.collapsingToolbarLayout.title = info.user.name
            AvatarLoader.loadAvatar(binding.avatar, info.user.image, R.drawable.ic_avatar_default)
            setUserDetails(info.user.details)
        }

        profileViewModel.offers.collectViewLifecycleAware {
            offersAdapter.submitData(it)
        }

        profileViewModel.error.collectViewLifecycleAware { event ->
            event.getContentOrNull()?.let { showSnackbarWithText(it.textRes) }
        }
    }

    private fun onOfferClick(offer: Offer) {
        val action = ProfileFragmentDirections.actionProfileFragmentToOfferFragment(offer.toArg())
        findNavController().navigate(action)
    }

    private fun setupToolbarIfNeeded() {
        if (!isCurrentUser) {
            binding.collapsingToolbarLayout.title = args.user?.name ?: ""
            ToolbarUtils.setupToolbar(
                activity,
                binding.toolbar,
                "",
                ToolbarUtils.NavIcon.UP
            )

            val actionBarSize = requireContext().getThemeDimension(R.attr.actionBarSize)
            binding.headerContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(
                    16.dp,
                    actionBarSize,
                    16.dp,
                    16.dp
                )
            }
            binding.collapsingToolbarLayout.expandedTitleMarginTop = 4.dp + actionBarSize
        }
    }

    private fun setUserDetails(userDetails: UserDetails?) {
        if (userDetails != null) {
            binding.textViewStatus.text = userDetails.status
            binding.textViewSignUpDate.text = userDetails.createdAt.toDescription()
            binding.textViewEmail.text = getString(R.string.profile_email, userDetails.email)
            binding.textViewStatus.isVisible = userDetails.status.isNotBlank()
            binding.signUpDateContainer.isVisible = true
            binding.emailContainer.isVisible = true
        } else {
            binding.textViewStatus.isVisible = false
            binding.signUpDateContainer.isVisible = false
            binding.emailContainer.isVisible = false
            binding.ageContainer.isVisible = false
        }
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