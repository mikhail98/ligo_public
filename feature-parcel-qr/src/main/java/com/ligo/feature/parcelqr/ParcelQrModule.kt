package com.ligo.feature.parcelqr

import org.koin.dsl.module

val ParcelQrModule = module {
    factory {
        ParcelQrFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}