package com.ligo.chats.coordinator

import android.net.Uri
import com.ligo.core.Initializable
import com.ligo.core.printError
import com.ligo.data.model.Attachment
import com.ligo.data.model.Chat
import com.ligo.data.model.MessageRequest
import com.ligo.data.model.User
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.chats.IChatsRepo
import com.ligo.data.socket.ISocketService
import com.ligo.data.socket.event.IncomingSocketEvent
import com.ligo.google.api.IStorageManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class ChatsCoordinator(
    private val chatsRepo: IChatsRepo,
    private val socketService: ISocketService,
    private val storageManager: IStorageManager,
    private val appPreferences: IAppPreferences,
) : IChatsCoordinator {

    private val unreadChatCountSubject: Subject<Int> = BehaviorSubject.create<Int>().toSerialized()

    private val chatListSubject: Subject<List<Chat>> =
        BehaviorSubject.create<List<Chat>>().toSerialized()

    private val messageReceivedSubject: Subject<String> =
        PublishSubject.create<String>().toSerialized()

    override val initOnList: List<Initializable.On> = listOf(Initializable.On.LOGIN)
    override val connectOnList: List<Initializable.On> = listOf(Initializable.On.NEVER)
    override val clearOnList: List<Initializable.On> = listOf(Initializable.On.LOGOUT)

    private var refreshChatsDisposable: Disposable? = null
    private var readMessagesDisposable: Disposable? = null
    private var sendMessageDisposable: Disposable? = null
    private var addChatDisposable: Disposable? = null
    private var socketDisposable: Disposable? = null

    private var user: User? = null
    private val chatMap: MutableMap<String, Chat> = mutableMapOf()

    override fun init() {
        user = appPreferences.getUser()
        refreshChats()

        socketDisposable?.dispose()
        socketService.getOnIncomingEventObservable()
            .subscribe(::handleEvents, ::printError)
            .also { socketDisposable = it }
    }

    private fun handleEvents(event: IncomingSocketEvent) {
        when (event) {
            is IncomingSocketEvent.MessageReceived -> {
                val chatId = event.message.chatId
                messageReceivedSubject.onNext(chatId)
                chatMap[chatId]?.updatedAt = event.message.createdAt
                chatMap[chatId]?.messages?.add(event.message)
                triggerRefreshChats()
            }

            is IncomingSocketEvent.MessagesWereRead -> {
                chatMap[event.chatId]?.messages?.forEach { message ->
                    if (message.userId == user?._id) message.isRead = true
                }
                triggerRefreshChats()
            }

            else -> Unit
        }
    }

    override fun clear() {
        refreshChatsDisposable?.dispose()
        readMessagesDisposable?.dispose()
        sendMessageDisposable?.dispose()
        addChatDisposable?.dispose()
        socketDisposable?.dispose()
        chatMap.clear()
        triggerRefreshChats()
    }

    override fun addOrUpdateChatByParcelId(parcelId: String) {
        chatsRepo.getChatByParcelId(parcelId).subscribeOn(Schedulers.io())
            .subscribe(
                { chat ->
                    chatMap[chat._id] = chat
                    triggerRefreshChats()
                },
                ::printError
            ).also { addChatDisposable = it }
    }

    override fun getChatForParcel(parcelId: String): Chat? {
        return chatMap.values.find { it.parcel._id == parcelId }
    }

    override fun readMessages(chatId: String) {
        chatsRepo.readMessages(chatId).subscribeOn(Schedulers.io()).subscribe(
            {
                chatMap[chatId]?.messages?.forEach { message ->
                    if (message.userId != user?._id) message.isRead = true
                }
                triggerRefreshChats()
            },
            ::printError
        ).also { readMessagesDisposable = it }
    }

    override fun readMessagesForParcel(parcelId: String) {
        readMessages(getChatIdForParcel(parcelId) ?: return)
    }

    override fun sendMessage(chatId: String, messageRequest: MessageRequest) {
        val userId = user?._id ?: return
        val chat = chatMap[chatId]

        val newMessage = messageRequest.toMessage(chatId, userId)
        if (chat != null) {
            chat.messages.add(newMessage)
            chat.updatedAt = newMessage.createdAt
            triggerRefreshChats()
        }

        val attachment = messageRequest.attachment
        val mediaUrl = attachment?.mediaUrl
        if (mediaUrl != null) {
            val email = user?.email
            val type = when (attachment.type) {
                Attachment.Type.PHOTO -> IStorageManager.FileType.PHOTO
                Attachment.Type.VIDEO -> IStorageManager.FileType.VIDEO
                Attachment.Type.AUDIO -> IStorageManager.FileType.AUDIO
                else -> null
            }
            if (email != null && type != null) {
                storageManager.uploadMedia(Uri.parse(attachment.mediaUrl), type, email)
                    .doOnSuccess {
                        messageRequest.attachment?.mediaUrl = it
                    }
                    .map {
                        messageRequest
                    }
            } else {
                Single.error(Throwable())
            }
        } else {
            Single.just(messageRequest)
        }.flatMap {
            chatsRepo.sendMessage(chatId, messageRequest).subscribeOn(Schedulers.io())
        }.subscribe(
            { message ->
                if (chat != null) {
                    val newMessageIndex = chat.messages.indexOfFirst { it.id == newMessage.id }
                    if (newMessageIndex != -1) {
                        chat.messages[newMessageIndex] = message
                        chat.updatedAt = message.createdAt
                        triggerRefreshChats()
                    }
                }
            },
            ::printError
        ).also { sendMessageDisposable = it }
    }

    private fun getChatIdForParcel(parcelId: String): String? {
        return chatMap.values.find { it.parcel._id == parcelId }?._id
    }

    override fun refreshChats() {
        chatsRepo.getChats().subscribeOn(Schedulers.io())
            .subscribe(
                { chats ->
                    chatMap.clear()
                    chats.forEach { chat -> chatMap[chat._id] = chat }
                    triggerRefreshChats()
                },
                ::printError
            ).also {
                refreshChatsDisposable = it
            }
    }

    override fun getMessageReceivedObservable(): Observable<String> {
        return messageReceivedSubject
    }

    override fun getChatsObservable(): Observable<List<Chat>> {
        return chatListSubject
    }

    override fun getUnreadChatCountObservable(): Observable<Int> {
        return unreadChatCountSubject
    }

    private fun triggerRefreshChats() {
        val unreadChatList = chatMap.values.filter {
            it.messages.any { message -> !message.isRead && message.userId != user?._id }
        }

        unreadChatCountSubject.onNext(unreadChatList.size)
        chatListSubject.onNext(chatMap.values.toList())
    }
}