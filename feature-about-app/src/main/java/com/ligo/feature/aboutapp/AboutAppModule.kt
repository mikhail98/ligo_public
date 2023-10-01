package com.ligo.feature.aboutapp

import org.koin.dsl.module

val AboutAppModule = module {
    factory {
        AboutAppFragmentViewModel(
            get(),
            get(),
            get()
        )
    }
}