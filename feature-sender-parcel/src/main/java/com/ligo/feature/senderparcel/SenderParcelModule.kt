package com.ligo.feature.senderparcel

import org.koin.dsl.module

val SenderParcelModule = module {
    factory {
        SenderParcelFragmentViewModel(
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