package com.ligo.subfeature.createtrip

import org.koin.dsl.module

val CreateTripModule = module {
    factory {
        CreateTripFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}