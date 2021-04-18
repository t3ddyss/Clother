package com.t3ddyss.clother.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.t3ddyss.clother.R
import com.t3ddyss.clother.adapters.ProfileOffersAdapter
import com.t3ddyss.clother.databinding.FragmentProfileBinding
import com.t3ddyss.clother.utilities.USER_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalPagingApi
class ProfileFragment : Fragment() {

    private val profileViewModel by viewModels<ProfileViewModel>()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var prefs: SharedPreferences

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val adapter = ProfileOffersAdapter(profileViewModel.user, viewLifecycleOwner) { }

        binding.list.layoutManager = layoutManager
        binding.list.adapter = adapter

        val horizontalDecorator = DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL)
        val verticalDecorator = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.apply {
            verticalDecorator.setDrawable(this)
            horizontalDecorator.setDrawable(this)

            binding.list.addItemDecoration(horizontalDecorator)
//            binding.list.addItemDecoration(verticalDecorator)
        }

        profileViewModel.offers.observe(viewLifecycleOwner) {
            lifecycleScope.launchWhenStarted {
                adapter.submitData(it)
            }
        }


        profileViewModel.getUser()
        profileViewModel.getOffers(prefs.getInt(USER_ID, 0))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}