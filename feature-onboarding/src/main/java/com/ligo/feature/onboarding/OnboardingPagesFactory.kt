package com.ligo.feature.onboarding

import com.ligo.data.model.ConfigStringKey
import com.ligo.navigator.api.OnboardingType

object OnboardingPagesFactory {

    fun createPages(type: OnboardingType = OnboardingType.MAIN): List<OnboardingPage> {
        return when (type) {
            OnboardingType.MAIN -> getDefaultOnboardingPages()
        }
    }

    private fun getDefaultOnboardingPages(): List<OnboardingPage> {
        return mutableListOf(
            OnboardingPage(
                ConfigStringKey.ONBOARDING_MAIN_PAGE_ONE_TITLE,
                ConfigStringKey.ONBOARDING_MAIN_PAGE_ONE_DESCRIPTION,
                R.drawable.content_onboarding_main_page_1
            ),
            OnboardingPage(
                ConfigStringKey.ONBOARDING_MAIN_PAGE_TWO_TITLE,
                ConfigStringKey.ONBOARDING_MAIN_PAGE_TWO_DESCRIPTION,
                R.drawable.content_onboarding_main_page_2
            ),
            OnboardingPage(
                ConfigStringKey.ONBOARDING_MAIN_PAGE_THREE_TITLE,
                ConfigStringKey.ONBOARDING_MAIN_PAGE_THREE_DESCRIPTION,
                R.drawable.content_onboarding_main_page_3
            )
        )
    }
}