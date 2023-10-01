package com.ligo.feature.chats

import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.common.model.ChatListUiModel
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable

class ChatsFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    appPreferences: IAppPreferences,
    private val chatsCoordinator: IChatsCoordinator,
) : BaseViewModel(navigator, analytics) {

    private val userId: String? = appPreferences.getUser()?._id

    fun getOnChatItemsObservable(): Observable<List<ChatListUiModel>> =
        chatsCoordinator.getChatsObservable()
            .map { chatList ->
                val userId = userId ?: return@map emptyList()
                chatList.asSequence()
                    .sortedBy { chat -> chat.updatedAt }
                    .map { chat -> ChatListUiModel.fromChat(userId, chat) }
                    .toList()
                    .reversed()
            }
}
