package com.ligo.subfeature.ratesender

import org.koin.dsl.module

val RateSenderModule = module {
    factory {
        RateSenderBottomSheetDialogFragmentViewModel(
            get(),
            get()
        )
    }
}