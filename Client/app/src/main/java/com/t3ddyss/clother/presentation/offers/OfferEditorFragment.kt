package com.t3ddyss.clother.presentation.offers

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.gson.JsonObject
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding
import com.t3ddyss.clother.util.text
import com.t3ddyss.clother.util.toCoordinatesString
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.showSnackbarWithAction
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.IntentUtils
import com.t3ddyss.core.util.utils.ToolbarUtils
import com.t3ddyss.feature_location.presentation.LocationSelectorFragment
import com.t3ddyss.navigation.util.observeNavigationResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfferEditorFragment
    : BaseFragment<FragmentOfferEditorBinding>(FragmentOfferEditorBinding::inflate) {
    private val viewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)
    private val args by navArgs<OfferEditorFragmentArgs>()

    private lateinit var requestGalleryPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var adapter: OfferEditorImagesAdapter
    private lateinit var layoutManager: LinearLayoutManager

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
        binding.textViewLocation.text = getString(R.string.location_select)
    }

    override fun onStart() {
        super.onStart()

        binding.location.setOnClickListener {
            val action = OfferEditorFragmentDirections
                .actionOfferEditorFragmentToLocationFragment(
                    calledFromId = R.id.offer_editor_graph
                )
            findNavController().navigate(action)
        }

        binding.buttonPublish.setOnClickListener {
            postOffer(args.category.id)
        }

        layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.listImages.layoutManager = layoutManager

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_large)?.apply {
            horizontalDecorator.setDrawable(this)
        }
        binding.listImages.addItemDecoration(horizontalDecorator)

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.images.observe(viewLifecycleOwner) { images ->
            adapter = OfferEditorImagesAdapter(images) {
                requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            binding.listImages.adapter = adapter
        }

        viewModel.location.observe(viewLifecycleOwner) {
            binding.textViewLocation.text = it.toCoordinatesString()
        }

        viewModel.newNewOfferResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Loading<*> ->
                    binding.layoutLoading.isVisible = true
                is Success<*> -> {
                    val action = OfferEditorFragmentDirections
                        .actionOfferEditorFragmentToHomeFragment(it.content as? Int ?: 0)
                    findNavController().navigate(action)
                    binding.layoutLoading.isVisible = false
                }
                is Error<*> -> {
                    binding.layoutLoading.isVisible = false
                    showSnackbarWithText(it)
                }
            }
        }

        observeNavigationResult<String>(LocationSelectorFragment.COORDINATES_KEY) {
            viewModel.selectLocation(it)
        }
    }

    // TODO move to ViewModel
    private fun postOffer(categoryId: Int) {
        val offer = JsonObject()
        offer.addProperty("category_id", categoryId)

        val title = binding.editTextTitle.text()
        if (title.isEmpty()) {
            binding.textInputTitle.error = getString(R.string.offer_title_requirement)
            return
        }
        offer.addProperty("title", title)
        binding.textInputTitle.isErrorEnabled = false

        val images = viewModel.images.value!!.toList()
        if (images.isEmpty()) {
            showSnackbarWithText(R.string.offer_image_requirement)
            return
        }

        val description = binding.editTextDescription.text()
        if (description.isNotEmpty()) {
            offer.addProperty("description", description)
        }

        val location = viewModel.location.value
        if (location != null) {
            offer.addProperty("location", "${location.latitude},${location.longitude}")
        }

        val checkedChipId = binding.chipGroupSize.chipGroupSize.checkedChipId
        if (checkedChipId != View.NO_ID) {
            val size = binding.chipGroupSize.chipGroupSize
                .findViewById<Chip>(checkedChipId).text.toString()
            offer.addProperty("size", size)
        }

        viewModel.postOffer(offer, images)
    }
}