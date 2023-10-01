package com.ligo.subfeature.createparcel.parceldetails

import org.koin.dsl.module

val ParcelDetailsModule = module {
    factory {
        ParcelDetailsBottomSheetDialogFragmentViewModel(
            get(),
            get(),
            get()
        )
    }
}