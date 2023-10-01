package com.ligo.subfeature.parceltype

import org.koin.dsl.module

val ParcelTypeModule = module {
    factory {
        ParcelTypeBottomSheetDialogFragmentViewModel(
            get(),
            get(),
            get()
        )
    }
}