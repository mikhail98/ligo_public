package com.ligo.feature.senderparcel

import org.koin.dsl.module

val ParcelDeliveredModule = module {
    factory {
        ParcelDeliveredBottomSheetDialogFragmentViewModel(
            get(),
            get()
        )
    }
}