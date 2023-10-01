package com.ligo.feature.profile

import org.koin.dsl.module

val ProfileModule = module {
    factory {
        ProfileFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
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