package com.t3ddyss.clother.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.common.common.Storage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val storage: Storage
) : ViewModel() {

    fun completeOnboarding() {
        storage.isOnboardingCompleted = true
    }

    companion object {
        val cards = listOf(
            OnboardingCard(
                title = R.string.onboarding_title_1,
                description = R.string.onboarding_description_1,
                icon = R.drawable.ic_onboarding_1
            ),
            OnboardingCard(
                title = R.string.onboarding_title_2,
                description = R.string.onboarding_description_2,
                icon = R.drawable.ic_onboarding_2
            ),
            OnboardingCard(
                title = R.string.onboarding_title_3,
                description = R.string.onboarding_description_3,
                icon = R.drawable.ic_onboarding_3
            )
        )
    }
}