package com.t3ddyss.clother.presentation.offers

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOfferBinding
import com.t3ddyss.clother.util.formatDate
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.errorText
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfferFragment : BaseFragment<FragmentOfferBinding>(FragmentOfferBinding::inflate){

    private val viewModel by activityViewModels<OfferViewModel>()
    private val args by navArgs<OfferFragmentArgs>()

    private val isCurrentUser get() = args.user.id == viewModel.userId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.offer),
            ToolbarUtils.NavIcon.UP
        )
        setHasOptionsMenu(true)

        if (isCurrentUser) {
            binding.user.isClickable = false
            binding.imageViewArrowUser.isInvisible = true
        } else {
            binding.user.setOnClickListener {
                findNavController().navigate(
                    OfferFragmentDirections.actionOfferFragmentToProfileFragment(args.user)
                )
            }
        }

        subscribeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_offer_menu, menu)
        menu.findItem(R.id.delete)?.isVisible = isCurrentUser
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
        viewModel.offer.observe(viewLifecycleOwner) {
            with(binding) {
                images.adapter = OfferImagesAdapter(it.images) { }
                if (it.images.size > 1) {
                    TabLayoutMediator(dots, images) { _, _ -> }.attach()
                } else {
                    binding.dots.isVisible = false
                }

                textViewTitle.text = it.title

                if (!it.description.isNullOrEmpty()) {
                    textViewDescription.text = it.description
                } else {
                    textViewDescription.isVisible = false
                }

                if (it.category.isNotEmpty()) {
                    textViewCategory.text = it.category
                } else {
                    groupCategory.isVisible = false
                }

                if (!it.location.isNullOrEmpty()) {
                    location.setOnClickListener { _ ->
                        val action = OfferFragmentDirections
                            .actionOfferFragmentToLocationViewerFragment(it.location)
                        findNavController().navigate(action)
                    }
                } else {
                    groupLocation.isVisible = false
                }

                if (!it.size.isNullOrEmpty()) {
                    textViewSize.text = it.size
                } else {
                    groupSize.isVisible = false
                }

                textViewUser.text = it.user.name
                textViewTime.text = it.createdAt.formatDate()
            }
        }

        viewModel.deletionResponse.observe(viewLifecycleOwner) {
            val result = it.getContentIfNotHandled() ?: return@observe

            when (result) {
                is Success<*> -> {
                    findNavController().popBackStack()
                    showSnackbarWithText(R.string.offer_deleted_message)
                }
                is Error<*> -> {
                    binding.layoutLoading.isVisible = false
                    showSnackbarWithText(result.errorText())
                }
                else -> {
                    binding.layoutLoading.isVisible = false
                }
            }
        }
    }

    private fun showDeletionConfirmationDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MaterialAlertDialogStyle
        )
            .setTitle(getString(R.string.offer_delete))
            .setMessage(getString(R.string.offer_deletion_confirmation))
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                binding.layoutLoading.isVisible = true
                viewModel.deleteOffer()
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }
}