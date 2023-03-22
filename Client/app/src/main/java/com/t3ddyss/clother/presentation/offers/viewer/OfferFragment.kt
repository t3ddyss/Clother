package com.t3ddyss.clother.presentation.offers.viewer

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOfferBinding
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.util.extensions.formatDate
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.extensions.textRes
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfferFragment : BaseFragment<FragmentOfferBinding>(FragmentOfferBinding::inflate){

    private val viewModel by viewModels<OfferViewModel>()
    private val args by navArgs<OfferFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.offer),
            ToolbarUtils.NavIcon.UP
        )
        setHasOptionsMenu(true)

        if (!viewModel.isOtherUser) {
            binding.user.isClickable = false
            binding.imageViewArrowUser.isInvisible = true
        } else {
            binding.user.setOnClickListener {
                findNavController().navigate(
                    OfferFragmentDirections.actionOfferFragmentToProfileFragment(args.offer.user)
                )
            }
        }

        subscribeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_offer_menu, menu)
        menu.findItem(R.id.delete)?.isVisible = !viewModel.isOtherUser
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                showDeletionConfirmationDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun subscribeUi() {
        viewModel.state.collectViewLifecycleAware { state ->
            setOffer(state.offer)

            when (state) {
                is OfferState.Initial -> {
                    binding.layoutLoading.isVisible = false
                }
                is OfferState.Loading -> {
                    binding.layoutLoading.isVisible = true
                }
                is OfferState.DeletionError -> {
                    binding.layoutLoading.isVisible = false
                }
                is OfferState.DeletionSuccess -> {
                    findNavController().popBackStack()
                    showSnackbarWithText(R.string.offer_deleted_message)
                }
            }
        }

        viewModel.error.collectViewLifecycleAware { event ->
            event.getContentOrNull()?.let { showSnackbarWithText(it.error.textRes) }
        }
    }

    private fun setOffer(offer: Offer) = with(binding) {
        images.adapter = OfferImagesAdapter(offer.images)

        if (offer.images.size > 1) {
            TabLayoutMediator(dots, images) { _, _ -> }.attach()
        } else {
            binding.dots.isVisible = false
        }

        textViewTitle.text = offer.title

        if (!offer.description.isNullOrEmpty()) {
            textViewDescription.text = offer.description
        } else {
            textViewDescription.isVisible = false
        }

        if (offer.category.isNotEmpty()) {
            textViewCategory.text = offer.category
        } else {
            groupCategory.isVisible = false
        }

        if (!offer.location.isNullOrEmpty()) {
            location.setOnClickListener {
                val action = OfferFragmentDirections
                    .actionOfferFragmentToLocationViewerFragment(offer.location)
                findNavController().navigate(action)
            }
        } else {
            groupLocation.isVisible = false
        }

        if (!offer.size.isNullOrEmpty()) {
            textViewSize.text = offer.size
        } else {
            groupSize.isVisible = false
        }

        textViewUser.text = offer.user.name
        textViewTime.text = offer.createdAt.formatDate()
    }

    private fun showDeletionConfirmationDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MaterialAlertDialogStyle
        )
            .setTitle(getString(R.string.offer_delete))
            .setMessage(getString(R.string.offer_deletion_confirmation))
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                viewModel.deleteOffer()
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }
}