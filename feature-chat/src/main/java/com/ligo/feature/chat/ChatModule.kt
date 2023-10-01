package com.ligo.feature.chat

import org.koin.dsl.module

val ChatModule = module {
    factory {
        ChatFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}