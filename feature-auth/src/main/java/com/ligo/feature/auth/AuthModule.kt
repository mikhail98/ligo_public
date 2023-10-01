package com.ligo.feature.auth

import org.koin.dsl.module

val AuthModule = module {
    factory {
        AuthFragmentViewModel(
            get(),
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