package com.t3ddyss.clother.ui.offer_editor

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OfferEditorImagesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding
import com.t3ddyss.clother.utilities.toCoordinatesString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File


@AndroidEntryPoint
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OfferEditorFragment : Fragment() {
    private val viewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)

    private var _binding: FragmentOfferEditorBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<OfferEditorFragmentArgs>()

    private lateinit var adapter: OfferEditorImagesAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferEditorBinding.inflate(inflater, container, false)

        val category = args.category
        binding.category.icon.isVisible = false
        binding.category.textViewTitle.text = category.title
        binding.textViewLocation.text = getString(R.string.select_location)

        val requestGalleryPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted ->
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

        layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        binding.listImages.layoutManager = layoutManager

        viewModel.images.observe(viewLifecycleOwner) { images ->
            adapter = OfferEditorImagesAdapter(images) {
                requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            binding.listImages.adapter = adapter
        }

        viewModel.location.observe(viewLifecycleOwner) {
            binding.textViewLocation.text = it.toCoordinatesString()
        }

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_large)?.apply {
            horizontalDecorator.setDrawable(this)
        }
        binding.listImages.addItemDecoration(horizontalDecorator)

        binding.location.setOnClickListener {
            findNavController().navigate(R.id.action_offerEditorFragment_to_locationFragment)
        }

        binding.buttonPublish.setOnClickListener {
            val offer = JsonObject()

            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()
//            val location = viewModel.location.value!!
//            val coordinates = "${location.latitude},${location.longitude}"
            val images = viewModel.images.value!!.toList()

            offer.addProperty("category_id", category.id)
            offer.addProperty("title", title)
            offer.addProperty("description", description)
//            json.addProperty("location", coordinates)

            viewModel.postOffer(offer, images)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}