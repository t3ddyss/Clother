package com.t3ddyss.clother.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingCard(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int
)
