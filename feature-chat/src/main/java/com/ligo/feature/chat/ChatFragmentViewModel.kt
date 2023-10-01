package com.ligo.feature.chat

import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.common.model.ChatUiModel
import com.ligo.data.model.Attachment
import com.ligo.data.model.Chat
import com.ligo.data.model.MessageRequest
import com.ligo.data.model.User
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.navigator.api.Target.ParcelInTrip
import com.ligo.navigator.api.Target.SenderParcel
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.PickPhoto
import io.reactivex.rxjava3.core.Observable
import java.util.Optional

class ChatFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    appPreferences: IAppPreferences,
    private val photoManager: IPhotoManager,
    private val chatsCoordinator: IChatsCoordinator,
) : BaseViewModel(navigator, analytics) {

    private var chatId: String? = null
    private val user: User? = appPreferences.getUser()
    private var phoneCallTarget: Target.PhoneCallApp? = null
    private var mapAppTarget: Target.MapApp? = null
    private var showParcelTarget: Target? = null

    override fun onCreate() {
        super.onCreate()
        photoManager.getOnPhotoPickedObservable().subscribeAndDispose(::handlePhoto)
    }

    fun sendMessage(text: String) {
        chatsCoordinator.sendMessage(chatId ?: return, MessageRequest(text = text))
    }

    fun readMessages(chatId: String) {
        if (this.chatId != chatId) return
        chatsCoordinator.readMessages(chatId)
    }

    private fun handlePhoto(pickedPhoto: PickPhoto) {
        if (pickedPhoto.origin != PickPhoto.Origin.CHAT_PHOTO) return

        val type = Attachment.Type.PHOTO
        val uri = pickedPhoto.uri.toString()
        val attachment = Attachment(type, uri)

        chatsCoordinator.sendMessage(chatId ?: return, MessageRequest(attachment = attachment))
    }

    fun getChatObservable(chatId: String): Observable<Optional<ChatUiModel>> {
        this.chatId = chatId

        readMessages(chatId)
        return chatsCoordinator.getChatsObservable()
            .map { chats ->
                val chat = chats.find { it._id == chatId }
                setUpPhoneNumber(chat)
                setUpMapAppTarget(chat)
                setUpShowParcelTarget(chat)

                Optional.ofNullable(ChatUiModel.fromChat(user?._id, chat))
            }
    }

    private fun setUpShowParcelTarget(chat: Chat?) {
        chat ?: return
        val parcelId = chat.parcel._id
        showParcelTarget =
            if (user?._id == chat.sender._id) SenderParcel(parcelId) else ParcelInTrip(parcelId)
    }

    private fun setUpPhoneNumber(chat: Chat?) {
        if (phoneCallTarget == null && chat != null) {
            phoneCallTarget = when (user?._id) {
                chat.sender._id -> Target.PhoneCallApp(chat.driver.phone)
                chat.driver._id -> Target.PhoneCallApp(chat.sender.phone)
                else -> null
            }
        }
    }

    private fun setUpMapAppTarget(chat: Chat?) {
        if (mapAppTarget == null && chat != null) {
            val location =
                if (user?._id == chat.sender._id) chat.driver.location else chat.sender.location
            location ?: return
            mapAppTarget = Target.MapApp(
                latitude = location.latitude,
                longitude = location.longitude,
                label = location.address.orEmpty()
            )
        }
    }

    fun call() = phoneCallTarget?.let(navigator::open)

    fun showLocation() = mapAppTarget?.let(navigator::open)

    fun showParcel() = showParcelTarget?.let(navigator::open)

    fun getMessageReceivedObservable(): Observable<String> =
        chatsCoordinator.getMessageReceivedObservable()

    fun sourceSelected(source: PickPhoto.Source) {
        photoManager.takePhoto(source, PickPhoto.Origin.CHAT_PHOTO)
    }
}
