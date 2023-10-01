package com.ligo.data.repo.chats

import com.ligo.data.api.ChatsApi
import com.ligo.data.model.Chat
import com.ligo.data.model.CreateMessageRequest
import com.ligo.data.model.Message
import com.ligo.data.model.MessageRequest
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.BaseRepo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class ChatsRepo(
    private val appPreferences: IAppPreferences,
    private val chatsApi: ChatsApi,
) : BaseRepo(), IChatsRepo {

    private fun getAuthToken(): String {
        return appPreferences.getUser()?.authToken.orEmpty()
    }

    override fun getChats(): Single<List<Chat>> {
        return chatsApi.getChats(authToken = getAuthToken())
            .proceedWithApiThrowable()
    }

    override fun getChatById(chatId: String): Single<Chat> {
        return chatsApi.getChatById(authToken = getAuthToken(), chatId)
            .proceedWithApiThrowable()
    }

    override fun getChatByParcelId(parcelId: String): Single<Chat> {
        return chatsApi.getChatByParcel(authToken = getAuthToken(), parcelId = parcelId)
            .proceedWithApiThrowable()
    }

    override fun sendMessage(chatId: String, messageRequest: MessageRequest): Single<Message> {
        val message = CreateMessageRequest(messageRequest)
        return chatsApi.sendMessage(authToken = getAuthToken(), chatId = chatId, message = message)
            .proceedWithApiThrowable()
    }

    override fun readMessages(chatId: String): Completable {
        return chatsApi.readMessages(authToken = getAuthToken(), chatId)
            .proceedWithApiThrowable()
    }
}
