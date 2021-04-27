package com.t3ddyss.clother.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.OffersAdapter
import com.t3ddyss.clother.databinding.FragmentProfileBinding
import com.t3ddyss.clother.ui.offer.OfferViewModel
import com.t3ddyss.clother.utilities.CURRENT_USER_ID
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject


@AndroidEntryPoint
class ProfileFragment : Fragment() {
    // Using activityViewModels delegate here to save data across different instances of ProfileFragment
    private val profileViewModel by activityViewModels<ProfileViewModel>()
    private val offerViewModel by activityViewModels<OfferViewModel>()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val adapter = OffersAdapter {
        offerViewModel.selectOffer(it)
        val action = ProfileFragmentDirections
                .actionProfileFragmentToOfferFragment(it.userId)
        findNavController().navigate(action)
    }
    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit

    @Inject lateinit var prefs: SharedPreferences

    @ExperimentalPagingApi
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val layoutManager = GridLayoutManager(context, 2)

        loadStateListener = {
            when (it.refresh) {
                is LoadState.Loading -> {
                    binding.shimmer.isVisible = adapter.itemCount < 1
                    binding.list.isVisible = adapter.itemCount > 0
                }

                is LoadState.NotLoading -> {
                    binding.shimmer.isVisible = false
                    binding.list.isVisible = true

                    if (it.append.endOfPaginationReached && adapter.itemCount < 1) {
                        binding.emptyState.isVisible = true
                    }
                }

                is LoadState.Error -> {
                    val error = (it.refresh as LoadState.Error).error

                    if (error is HttpException && error.code() == 401) {
                        findNavController().navigate(R.id.action_global_signUpFragment)

                        (activity as? MainActivity)
                                ?.showGenericMessage(getString(R.string.session_expired))
                        prefs.edit().remove(IS_AUTHENTICATED).apply()
                    }
                    else {
                        binding.shimmer.isVisible = false
                        binding.list.isVisible = true
                        (activity as? MainActivity)
                                ?.showGenericMessage(error)
                    }
                }
            }
        }
        adapter.addLoadStateListener(loadStateListener)

        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.apply {
            horizontalDecorator.setDrawable(this)
            binding.list.addItemDecoration(horizontalDecorator)
        }

        profileViewModel.offers.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                adapter.submitData(it)
            }
        }

        offerViewModel.removedOffers.observe(viewLifecycleOwner) {
            profileViewModel.removeOffers(it)
        }

        profileViewModel.getOffers(prefs.getInt(CURRENT_USER_ID, 0))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.removeLoadStateListener(loadStateListener)
        _binding = null
    }
}