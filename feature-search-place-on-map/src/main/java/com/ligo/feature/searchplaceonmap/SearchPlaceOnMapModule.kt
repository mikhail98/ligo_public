package com.ligo.feature.searchplaceonmap

import org.koin.dsl.module

val SearchPlaceOnMapModule = module {
    factory {
        SearchPlaceOnMapFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}