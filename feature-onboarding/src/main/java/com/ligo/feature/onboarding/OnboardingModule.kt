package com.ligo.feature.onboarding

import org.koin.dsl.module

val OnboardingModule = module {
    factory {
        OnboardingFragmentViewModel(
            get(),
            get()
        )
    }
}