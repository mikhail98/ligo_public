package com.ligo.feature.delivery

import org.koin.dsl.module

val DeliveryModule = module {
    factory {
        DeliveryFragmentViewModel(
            get(),
            get()
        )
    }
}