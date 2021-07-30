package com.t3ddyss.clother.ui.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentFiltersDialogBinding
import com.t3ddyss.clother.ui.search_results.SearchResultsViewModel
import com.t3ddyss.clother.utilities.toCoordinatesString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterDialogFragment : BottomSheetDialogFragment() {
    private val searchViewModel by hiltNavGraphViewModels<SearchResultsViewModel>(
        R.id.search_results_graph
    )
    private val viewModel by hiltNavGraphViewModels<FiltersViewModel>(
        R.id.search_results_graph
    )
    private var _binding: FragmentFiltersDialogBinding? = null
    private val binding get() = _binding!!

    private var location: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFiltersDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // BottomSheetDialog will close after navigating back from LocationSelectorFragment,
        // but https://issuetracker.google.com/issues/134089818#comment7 says that
        // it is actually an expected BottomSheetDialog behaviour
        binding.cardViewLocation.setOnClickListener {
            val action = FilterDialogFragmentDirections
                .actionFilterDialogFragmentToLocationFragment(
                    calledFromId = R.id.search_results_graph
                )
            findNavController().navigate(action)
        }

        // TODO add ability to clear filters
        binding.buttonApply.setOnClickListener {
            val distance = getSelectedDistance()
            val size = getSelectedSize()

            if (location != null && distance != null) {
                searchViewModel.location.value = Pair(location!!, distance)
            } else {
                viewModel.maxDistance.value = View.NO_ID
            }

            if (size != null) {
                searchViewModel.size.value = size
            }

            findNavController().popBackStack()
        }

        viewModel.getSavedLocation()
        subscribeUi()
    }

    override fun onStart() {
        super.onStart()

        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeUi() {
        viewModel.location.observe(viewLifecycleOwner) {
            location = it
            binding.textViewLocation.text = it.toCoordinatesString()
        }

        viewModel.maxDistance.observe(viewLifecycleOwner) {
            if (it == View.NO_ID) return@observe

            binding.chipGroupDistance.chipGroupDistance.check(it)
        }

        viewModel.size.observe(viewLifecycleOwner) {
            if (it == View.NO_ID) return@observe

            binding.chipGroupSize.chipGroupSize.check(it)
        }
    }

    private fun getSelectedDistance(): Int? {
        val checkedChipId = binding.chipGroupDistance.chipGroupDistance.checkedChipId
        viewModel.maxDistance.value = checkedChipId

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
        viewModel.size.value = checkedChipId

        if (checkedChipId != View.NO_ID) {
            return binding.chipGroupSize.chipGroupSize
                .findViewById<Chip>(checkedChipId).text.toString()
        }
        return null
    }
}