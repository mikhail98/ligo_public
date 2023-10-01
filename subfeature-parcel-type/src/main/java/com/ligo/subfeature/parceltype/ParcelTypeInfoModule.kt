package com.ligo.subfeature.parceltype

import org.koin.dsl.module

val ParcelTypeInfoModule = module {
    factory {
        ParcelTypeInfoBottomSheetDialogFragmentViewModel(
            get(),
            get(),
            get()
        )
    }
}