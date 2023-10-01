package com.ligo.subfeature.createparcel.currency

import org.koin.dsl.module

val CurrencySelectionModule = module {
    factory {
        CurrencySelectionBottomSheetDialogFragmentViewModel(
            get(),
            get(),
            get()
        )
    }
}