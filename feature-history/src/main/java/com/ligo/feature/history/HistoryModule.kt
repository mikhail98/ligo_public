package com.ligo.feature.history

import org.koin.dsl.module

val HistoryModule = module {
    factory {
        HistoryFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }

    factory {
        ParcelInfoBottomSheetDialogFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }

    factory {
        RejectedParcelInfoBottomSheetViewModel(
            get(),
            get(),
            get()
        )
    }
}