package com.ligo.feature.history

import org.koin.dsl.module

val ParcelInfoModule = module {
    factory {
        ParcelInfoBottomSheetDialogFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}