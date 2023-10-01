package com.ligo.common.model

import com.ligo.core.getFormattedDate
import com.ligo.core.getTimeInMillis
import com.ligo.data.model.Chat

data class ChatUiModel(
    val userName: String,
    val avatarUrl: String?,
    val startPoint: String,
    val endPoint: String,
    val isDriver: Boolean,
    val messageList: List<BaseChatMessageUiModel>,
) {
    companion object {
        fun fromChat(userId: String?, chat: Chat?): ChatUiModel? {
            val userId = userId ?: return null
            val chat = chat ?: return null
            val (avatarUrl, userName) = when (userId) {
                chat.sender._id -> chat.driver.avatarPhoto to chat.driver.name
                chat.driver._id -> chat.sender.avatarPhoto to chat.sender.name
                else -> return null
            }

            val startPoint = chat.parcel.startPoint
            val endPoint = chat.parcel.endPoint

            var tmpDate = ""
            val messageUiItems = mutableListOf<BaseChatMessageUiModel>()
            chat.messages.forEach { message ->
                val date = getFormattedDate(getTimeInMillis(message.createdAt), "dd MMMM")
                if (tmpDate != date) {
                    tmpDate = date
                    messageUiItems.add(ChatMessageDateUiModel(message.createdAt))
                }

                when (message.userId) {
                    userId -> OutChatMessageUiModel.fromMessage(message)
                    else -> InChatMessageUiModel.fromMessage(message)
                }?.let {
                    messageUiItems.add(it)
                }
            }

            return ChatUiModel(
                userName = userName,
                avatarUrl = avatarUrl,
                startPoint = startPoint.cityName ?: startPoint.address.orEmpty(),
                endPoint = endPoint.cityName ?: endPoint.address.orEmpty(),
                isDriver = userId == chat.driver._id,
                messageList = messageUiItems
            )
        }
    }
}