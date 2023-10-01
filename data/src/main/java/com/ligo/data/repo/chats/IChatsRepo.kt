package com.ligo.data.repo.chats

import com.ligo.data.model.Chat
import com.ligo.data.model.Message
import com.ligo.data.model.MessageRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IChatsRepo {

    fun getChats(): Single<List<Chat>>

    fun getChatById(chatId: String): Single<Chat>

    fun getChatByParcelId(parcelId: String): Single<Chat>

    fun sendMessage(chatId: String, message: MessageRequest): Single<Message>

    fun readMessages(chatId: String): Completable
}