package com.t3ddyss.clother.presentation.offers.editor

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.util.extensions.text
import com.t3ddyss.clother.util.extensions.toCoordinatesString
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.extensions.showSnackbarWithAction
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.extensions.textRes
import com.t3ddyss.core.util.utils.IntentUtils
import com.t3ddyss.core.util.utils.ToolbarUtils
import com.t3ddyss.feature_location.presentation.LocationSelectorFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfferEditorFragment
    : BaseFragment<FragmentOfferEditorBinding>(FragmentOfferEditorBinding::inflate) {
    private val viewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)
    private val args by navArgs<OfferEditorFragmentArgs>()

    private lateinit var requestGalleryPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var adapter: OfferEditorImagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requestGalleryPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    findNavController().navigate(R.id.action_offerEditorFragment_to_galleryFragment)
                } else {
                    showSnackbarWithAction(
                        text = R.string.error_no_files_access,
                        actionText = R.string.action_grant_access,
                        action = { IntentUtils.openApplicationSettings(requireContext()) }
                    )
                }
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.menu_new_offer),
            ToolbarUtils.NavIcon.CLOSE
        )

        binding.category.icon.isVisible = false
        binding.category.textViewTitle.text = args.category.title

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_large)?.apply {
            horizontalDecorator.setDrawable(this)
        }
        binding.listImages.addItemDecoration(horizontalDecorator)

        binding.location.setOnClickListener {
            val action = OfferEditorFragmentDirections.actionOfferEditorFragmentToLocationFragment()
            findNavController().navigate(action)
        }

        binding.buttonPublish.setOnClickListener {
            viewModel.postOffer(
                title = binding.editTextTitle.text(),
                description = binding.editTextDescription.text(),
                size = binding.chipGroupSize.chipGroupSize.run {
                    if (checkedChipId == View.NO_ID) {
                        null
                    } else {
                        findViewById<Chip>(checkedChipId).text.toString()
                    }
                }
            )
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.images.collectViewLifecycleAware { images ->
            adapter = OfferEditorImagesAdapter(images.toMutableList()) {
                requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            binding.listImages.adapter = adapter
        }

        viewModel.location.collectViewLifecycleAware { latLng ->
            binding.textViewLocation.text = latLng?.toCoordinatesString() ?: getString(R.string.location_select)
        }

        viewModel.state.collectViewLifecycleAware { state ->
            binding.textInputTitle.isErrorEnabled = false

            when (state) {
                OfferEditorState.Loading -> {
                    binding.layoutLoading.isVisible = true
                }
                is OfferEditorState.ValidationError -> {
                    setValidationError(state)
                }
                OfferEditorState.Success -> {
                    val action = OfferEditorFragmentDirections.actionOfferEditorFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
                OfferEditorState.Error -> {
                    binding.layoutLoading.isVisible = false
                }
            }
        }

        viewModel.error.collectViewLifecycleAware { event ->
            event.getContentOrNull()?.let { error ->
                when (error) {
                    OfferEditorError.NoImagesSelected -> {
                        showSnackbarWithText(R.string.offer_image_requirement)
                    }
                    is OfferEditorError.Network -> {
                        showSnackbarWithText(error.cause.textRes)
                    }
                }
            }
        }

        setFragmentResultListener(LocationSelectorFragment.COORDINATES_KEY) { _, bundle ->
            bundle.getParcelable<LatLng>(LocationSelectorFragment.COORDINATES)?.let { latLng ->
                viewModel.selectLocation(latLng)
            }
        }
    }

    private fun setValidationError(state: OfferEditorState.ValidationError) {
        state.causes.forEach { param ->
            when (param) {
                OffersInteractor.OfferParam.TITLE -> {
                    binding.textInputTitle.error = getString(R.string.offer_title_requirement)
                    binding.textInputTitle.isErrorEnabled = true
                }
                OffersInteractor.OfferParam.IMAGES -> Unit
            }
        }
    }
}