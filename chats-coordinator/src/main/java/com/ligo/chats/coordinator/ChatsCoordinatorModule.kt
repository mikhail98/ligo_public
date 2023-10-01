package com.ligo.chats.coordinator

import org.koin.dsl.module

val ChatsCoordinatorModule = module {
    single<IChatsCoordinator> {
        ChatsCoordinator(
            get(),
            get(),
            get(),
            get()
        )
    }
}