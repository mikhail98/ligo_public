package com.ligo.feature.parcelintrip

import org.koin.dsl.module

val ConfirmPickupParcelModule = module {
    factory {
        ConfirmPickupParcelBottomSheetDialogFragmentViewModel(
            get(),
            get()
        )
    }
}