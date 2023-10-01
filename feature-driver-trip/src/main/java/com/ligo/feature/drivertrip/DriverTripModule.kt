package com.ligo.feature.drivertrip

import org.koin.dsl.module

val DriverTripModule = module {
    factory {
        DriverTripFragmentViewModel(
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