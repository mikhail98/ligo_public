package com.ligo.feature.searchplace

import org.koin.dsl.module

val SearchPlaceModule = module {
    factory {
        SearchPlaceFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}