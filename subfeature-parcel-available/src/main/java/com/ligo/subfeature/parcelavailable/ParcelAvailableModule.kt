package com.ligo.subfeature.parcelavailable

import org.koin.dsl.module

val ParcelAvailableModule = module {
    factory {
        ParcelAvailableBottomSheetDialogFragmentViewModel(
            get(),
            get(),
            get()
        )
    }
}