package com.ligo.feature.setupphone

import org.koin.dsl.module

val SetupPhoneModule = module {
    factory {
        SetupPhoneFragmentViewModel(
            get(),
            get(),
            get()
        )
    }
}