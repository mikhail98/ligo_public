package com.ligo.feature.searchfordriver

import org.koin.dsl.module

val SearchForDriverModule = module {
    factory {
        SearchForDriverFragmentViewModel(
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