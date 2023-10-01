package com.ligo.feature.main

import org.koin.dsl.module

val MainModule = module {
    factory {
        MainActivityViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}