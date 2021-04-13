package com.t3ddyss.clother.ui.chat

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentChatBinding
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChatFragment : Fragment() {

    private val viewModel by viewModels<ChatViewModel>()
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var prefs: SharedPreferences
    private val args by navArgs<ChatFragmentArgs>()

    private lateinit var loadStateListener: (CombinedLoadStates) -> Unit


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false)

        loadStateListener = {
            when (it.refresh) {
                is LoadState.Loading -> {
                    binding.layoutLoading.isVisible = true
                }

                is LoadState.NotLoading -> {
                    binding.layoutLoading.isVisible = false

//                    binding.emptyState.isVisible = it.append.endOfPaginationReached
//                                                   && adapter.itemCount < 1
                }

                is LoadState.Error -> {
                    val error = (it.refresh as LoadState.Error).error

                    if (error is HttpException && error.code() == 401) {
                        findNavController().navigate(R.id.action_searchResults_to_signUpFragment)

                        (activity as? MainActivity)
                                ?.showGenericError(getString(R.string.session_expired))
                        prefs.edit().remove(IS_AUTHENTICATED).apply()
                    }
                    else {
                        binding.layoutLoading.isVisible = false

                        (activity as? MainActivity)
                                ?.showGenericError(error)
                    }
                }
            }

            // Hide footer with progress bar
//            if (it.append !is LoadState.Loading) {
//                binding.progressBarFooter.isVisible = false
//                viewModel.endOfPaginationReachedBottom = it.append.endOfPaginationReached
//
//                // Disable bottom padding when end of pagination is reached
//                if (it.append.endOfPaginationReached) {
//                    binding.list.setPadding(0, 0, 0, 0)
//                }
//            }
        }
//        adapter.addLoadStateListener(loadStateListener)

        binding.listMessages.layoutManager = layoutManager
//        binding.listMessages.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}