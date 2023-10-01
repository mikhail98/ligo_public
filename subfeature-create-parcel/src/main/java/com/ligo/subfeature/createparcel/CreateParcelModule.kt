package com.ligo.subfeature.createparcel

import org.koin.dsl.module

val CreateParcelModule = module {
    factory {
        CreateParcelFragmentViewModel(
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