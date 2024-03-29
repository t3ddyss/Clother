package com.t3ddyss.clother.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.DialogSearchFiltersBinding
import com.t3ddyss.clother.util.extensions.toCoordinatesString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterDialogFragment : BottomSheetDialogFragment() {
    private val searchViewModel by hiltNavGraphViewModels<SearchResultsViewModel>(
        R.id.search_results_graph
    )
    private val viewModel by hiltNavGraphViewModels<FiltersViewModel>(
        R.id.search_results_graph
    )
    private var _binding: DialogSearchFiltersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSearchFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Using onStart() because of navGraphViewModels
    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // BottomSheetDialog will close after navigating back from LocationSelectorFragment,
        // but https://issuetracker.google.com/issues/134089818#comment7 says that
        // it is actually an expected BottomSheetDialog behaviour
        binding.cardViewLocation.setOnClickListener {
            val action = FilterDialogFragmentDirections
                .actionFilterDialogFragmentToLocationFragment()
            findNavController().navigate(action)
        }

        binding.buttonApply.setOnClickListener {
            val location = viewModel.location.value
            val radius = getSelectedRadius()
            val size = getSelectedSize()

            if (location != null && radius != null) {
                searchViewModel.location.value = Pair(location, radius)
            } else {
                searchViewModel.location.value = null
                viewModel.onRadiusSelected(null)
            }

            if (size != null) {
                searchViewModel.size.value = size
            } else {
                searchViewModel.size.value = null
            }

            searchViewModel.filters.value = Unit
            findNavController().popBackStack()
        }

        binding.buttonClear.setOnClickListener {
            viewModel.onRadiusSelected(null)
            viewModel.onSizeSelected(null)
            searchViewModel.location.value = null
            searchViewModel.size.value = null
            searchViewModel.filters.value = Unit
            findNavController().popBackStack()
        }

        subscribeUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeUi() {
        viewModel.location.observe(viewLifecycleOwner) { latLng ->
            binding.textViewLocation.text = latLng?.toCoordinatesString()
                ?: getString(R.string.search_set_location)
        }

        viewModel.radius.observe(viewLifecycleOwner) { chipId ->
            if (chipId != null) {
                binding.chipGroupDistance.chipGroupDistance.check(chipId)
            } else {
                binding.chipGroupDistance.chipGroupDistance.clearCheck()
            }
        }

        viewModel.size.observe(viewLifecycleOwner) { chipId ->
            if (chipId != null) {
                binding.chipGroupSize.chipGroupSize.check(chipId)
            } else {
                binding.chipGroupSize.chipGroupSize.clearCheck()
            }
        }
    }

    private fun getSelectedRadius(): Int? {
        val checkedChipId = binding.chipGroupDistance.chipGroupDistance.checkedChipId
        viewModel.onRadiusSelected(checkedChipId)

        return when (checkedChipId) {
            R.id.dist_5km -> 5
            R.id.dist_10km -> 10
            R.id.dist_25km -> 25
            R.id.dist_50km -> 50
            else -> null
        }
    }

    private fun getSelectedSize(): String? {
        val checkedChipId = binding.chipGroupSize.chipGroupSize.checkedChipId
        viewModel.onSizeSelected(checkedChipId)

        return if (checkedChipId != View.NO_ID) {
            binding.chipGroupSize.chipGroupSize.findViewById<Chip>(checkedChipId).text.toString()
        } else {
            null
        }
    }
}