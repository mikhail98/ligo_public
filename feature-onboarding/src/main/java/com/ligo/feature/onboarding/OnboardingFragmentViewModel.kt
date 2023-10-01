package com.ligo.feature.onboarding

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator

class OnboardingFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
) : BaseViewModel(navigator, analytics)