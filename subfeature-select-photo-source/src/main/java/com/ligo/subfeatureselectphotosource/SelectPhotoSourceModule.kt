package com.ligo.subfeatureselectphotosource

import org.koin.dsl.module

val SelectPhotoSourceModule = module {
    factory {
        SelectPhotoSourceBottomSheetDialogFragmentViewModel(
            get(),
            get()
        )
    }
}