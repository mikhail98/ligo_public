package com.ligo.feature.parcelintrip

import org.koin.dsl.module

val ParcelInTripModule = module {

    factory {
        ParcelInTripFragmentViewModel(
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