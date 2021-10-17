package com.t3ddyss.clother.ui.offer

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OfferImagesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferBinding
import com.t3ddyss.clother.models.domain.*
import com.t3ddyss.clother.ui.BaseFragment
import com.t3ddyss.clother.utilities.CURRENT_USER_ID
import com.t3ddyss.clother.utilities.formatDate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OfferFragment : BaseFragment<FragmentOfferBinding>(FragmentOfferBinding::inflate){

    private val viewModel by activityViewModels<OfferViewModel>()
    private val args by navArgs<OfferFragmentArgs>()

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentUserId = prefs.getInt(CURRENT_USER_ID, 0)
        setHasOptionsMenu(args.posterId == currentUserId)

        subscribeUi(currentUserId)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete) {
            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog
            )
                .setTitle(getString(R.string.delete_offer))
                .setMessage(getString(R.string.deletion_confirmation))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    binding.layoutLoading.isVisible = true
                    viewModel.deleteOffer()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeUi(currentUserId: Int) {
        viewModel.offer.observe(viewLifecycleOwner) {
            with(binding) {
                images.adapter = OfferImagesAdapter(it.images) {
                }
                if (it.images.size > 1) {
                    TabLayoutMediator(dots, images) { _, _ ->
                    }.attach()
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

                textViewUser.text = it.userName
                textViewTime.text = it.createdAt.formatDate()

                if (it.userId == currentUserId) {
                    buttonMessage.isVisible = false
                    return@observe
                }

                // TODO add User to Offer
                buttonMessage.setOnClickListener { _ ->
                    val action = OfferFragmentDirections
                        .actionOfferFragmentToChatFragment(User(it.userId, it.userName))
                    findNavController().navigate(action)
                }
            }
        }

        viewModel.deletionResponse.observe(viewLifecycleOwner) {
            val result = it.getContentIfNotHandled() ?: return@observe

            when (result) {
                is Success<*> -> {
                    findNavController().popBackStack()
                    showGenericMessage(getString(R.string.offer_deleted))
                }
                is Error<*> -> {
                    binding.layoutLoading.isVisible = false
                    showGenericMessage(result.message)
                }
                is Failed<*> -> {
                    binding.layoutLoading.isVisible = false
                    showGenericMessage(getString(R.string.no_connection))
                }
                else -> {
                    binding.layoutLoading.isVisible = false
                }
            }
        }
    }
}