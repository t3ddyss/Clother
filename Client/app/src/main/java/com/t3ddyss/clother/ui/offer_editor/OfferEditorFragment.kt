package com.t3ddyss.clother.ui.offer_editor

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OfferEditorImagesAdapter
import com.t3ddyss.clother.databinding.FragmentOfferEditorBinding
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@ExperimentalPagingApi
class OfferEditorFragment : Fragment() {
    private val offerEditorViewModel by viewModels<OfferEditorViewModel>()
    private var _binding: FragmentOfferEditorBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OfferEditorImagesAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferEditorBinding.inflate(inflater, container, false)

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d(DEBUG_TAG, "Permission granted")
                } else {
                    Log.d(DEBUG_TAG, "Permission denied")
                }
            }

        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        offerEditorViewModel.images.observe(viewLifecycleOwner) { images ->
            adapter = OfferEditorImagesAdapter(images, {
                Log.d(DEBUG_TAG, "Detach image $it")
            },
            {
                Log.d(DEBUG_TAG, "Attach image")
                findNavController().navigate(R.id.action_offerEditorFragment_to_galleryFragment)
            })

            layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
            )

            binding.listImages.layoutManager = layoutManager
            binding.listImages.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}