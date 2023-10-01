package com.ligo.subfeature.createtrip

import org.koin.dsl.module

val DatePickerModule = module {
    factory {
        DatePickerBottomSheetDialogFragmentViewModel(
            get(),
            get()
        )
    }
}