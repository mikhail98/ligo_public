package com.ligo.feature.chats

import org.koin.dsl.module

val ChatsModule = module {
    factory {
        ChatsFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}