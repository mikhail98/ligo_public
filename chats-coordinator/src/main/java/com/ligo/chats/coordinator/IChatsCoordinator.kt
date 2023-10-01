package com.ligo.chats.coordinator

import com.ligo.core.Initializable
import com.ligo.data.model.Chat
import com.ligo.data.model.MessageRequest
import io.reactivex.rxjava3.core.Observable

interface IChatsCoordinator : Initializable {

    fun refreshChats()

    fun addOrUpdateChatByParcelId(parcelId: String)

    fun getChatForParcel(parcelId: String): Chat?

    fun sendMessage(chatId: String, messageRequest: MessageRequest)

    fun readMessages(chatId: String)

    fun readMessagesForParcel(parcelId: String)

    fun getMessageReceivedObservable(): Observable<String>

    fun getChatsObservable(): Observable<List<Chat>>

    fun getUnreadChatCountObservable(): Observable<Int>
}
