package com.t3ddyss.clother.ui.offer_editor

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OfferEditorImagesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@ExperimentalPagingApi
class OfferEditorFragment : Fragment() {
    private val galleryViewModel by hiltNavGraphViewModels<OfferEditorViewModel>(R.id.offer_editor_graph)

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

        val openSettingsAction = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        val requestGalleryPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted ->
                if (isGranted) {
                    findNavController().navigate(R.id.action_offerEditorFragment_to_galleryFragment)
                } else {
                    (activity as? MainActivity)
                            ?.showSnackbarWithAction(
                                    message = getString(R.string.no_gallery_access),
                                    actionText = getString(R.string.grant_access),
                                    action = openSettingsAction
                            )
                }
            }

        layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        binding.listImages.layoutManager = layoutManager

        galleryViewModel.images.observe(viewLifecycleOwner) { images ->
            adapter = OfferEditorImagesAdapter(images.filter { it.isSelected }.toMutableList()
            ) {
                requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            binding.listImages.adapter = adapter
        }

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider_large)?.apply {
            horizontalDecorator.setDrawable(this)
        }

        binding.listImages.addItemDecoration(horizontalDecorator)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}