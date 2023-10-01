package com.ligo.feature.splash

import org.koin.dsl.module

val SplashModule = module {
    factory {
        SplashFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}