package com.ligo.feature.camera

import org.koin.dsl.module

val CameraModule = module {
    factory {
        CameraFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}