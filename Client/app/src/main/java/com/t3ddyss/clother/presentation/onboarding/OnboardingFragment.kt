package com.t3ddyss.clother.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentOnboardingBinding
import com.t3ddyss.core.presentation.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment
    : BaseFragment<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {

    private val viewModel by viewModels<OnboardingViewModel>()
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OnboardingAdapter(OnboardingViewModel.cards)
        binding.cards.adapter = adapter
        TabLayoutMediator(binding.dots, binding.cards) { _, _ -> }.attach()
        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupControls(position)
            }
        }.also {
            binding.cards.registerOnPageChangeCallback(it)
        }

        binding.buttonNext.setOnClickListener {
            val pageIndex = binding.cards.currentItem
            binding.cards.currentItem = pageIndex + 1
        }
        binding.buttonSkip.setOnClickListener {
            navigateToSignUpScreen()
        }
        binding.buttonComplete.setOnClickListener {
            navigateToSignUpScreen()
        }
    }

    override fun onDestroyView() {
        pageChangeCallback?.let {
            binding.cards.unregisterOnPageChangeCallback(it)
        }
        pageChangeCallback = null
        super.onDestroyView()
    }

    private fun setupControls(position: Int) {
        val isLastCard = isLastCard(position)
        binding.controlsGroup.isInvisible = isLastCard
        binding.buttonComplete.isVisible = isLastCard
    }

    private fun isLastCard(position: Int): Boolean {
        return position == OnboardingViewModel.cards.lastIndex
    }

    private fun navigateToSignUpScreen() {
        viewModel.completeOnboarding()
        findNavController().navigate(R.id.action_onboardingFragment_to_signUpFragment)
    }
}