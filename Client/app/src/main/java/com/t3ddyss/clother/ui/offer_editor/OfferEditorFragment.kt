package com.t3ddyss.clother.ui.offer_editor

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
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OfferEditorImagesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding
import com.t3ddyss.clother.models.domain.*
import com.t3ddyss.clother.ui.BaseFragment
import com.t3ddyss.clother.utilities.text
import com.t3ddyss.clother.utilities.toCoordinatesString
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
                    (activity as? MainActivity)
                        ?.showSnackbarWithAction(
                            message = getString(R.string.no_gallery_access),
                            actionText = getString(R.string.grant_access)
                        )
                }
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val category = args.category
        binding.category.icon.isVisible = false
        binding.category.textViewTitle.text = category.title
        binding.textViewLocation.text = getString(R.string.select_location)

        binding.location.setOnClickListener {
            val action = OfferEditorFragmentDirections
                .actionOfferEditorFragmentToLocationFragment(
                    calledFromId = R.id.offer_editor_graph
                )
            findNavController().navigate(action)
        }

        binding.buttonPublish.setOnClickListener {
            postOffer(category)
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
                    (activity as? MainActivity)?.showGenericMessage(it.message)
                }
                is Failed<*> -> {
                    binding.layoutLoading.isVisible = false
                    (activity as? MainActivity)?.showGenericMessage(getString(R.string.no_connection))
                }
            }
        }
    }

    private fun postOffer(categoryEntity: Category) {
        val offer = JsonObject()
        offer.addProperty("category_id", categoryEntity.id)

        val title = binding.editTextTitle.text()
        if (title.isEmpty()) {
            binding.textInputTitle.error = getString(R.string.provide_title)
            return
        }
        offer.addProperty("title", title)
        binding.textInputTitle.isErrorEnabled = false

        val images = viewModel.images.value!!.toList()
        if (images.isEmpty()) {
            (activity as? MainActivity)?.showGenericMessage(getString(R.string.provide_image))
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