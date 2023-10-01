package com.ligo.feature.rejectparcel

import org.koin.dsl.module

val RejectParcelModule = module {
    factory {
        RejectParcelFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}