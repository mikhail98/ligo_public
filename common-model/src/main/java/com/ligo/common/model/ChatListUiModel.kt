package com.ligo.common.model

import com.ligo.core.getFormattedDate
import com.ligo.core.getTimeInMillis
import com.ligo.data.MessageReadStatus
import com.ligo.data.model.Attachment
import com.ligo.data.model.Chat

data class ChatListUiModel(
    val chatId: String,
    val title: String,
    val avatarUrl: String?,
    val messageText: String?,
    val attachment: Attachment?,
    val chatTime: String,
    val status: MessageReadStatus?,
    val unreadCount: Int,
) {
    companion object {

        fun fromChat(userId: String, chat: Chat): ChatListUiModel {
            val title = when (userId) {
                chat.sender._id -> chat.driver.name
                chat.driver._id -> chat.sender.name
                else -> ""
            }

            val time = buildItemDate(chat.updatedAt)
            val lastMessage = chat.messages.lastOrNull()

            val avatarUrl = when (userId) {
                chat.sender._id -> chat.driver.avatarPhoto
                chat.driver._id -> chat.sender.avatarPhoto
                else -> null
            }

            val status = if (lastMessage?.userId == userId) {
                if (lastMessage.isRead) MessageReadStatus.READ else MessageReadStatus.SENT
            } else {
                null
            }

            return ChatListUiModel(
                chatId = chat._id,
                title = title,
                avatarUrl = avatarUrl,
                messageText = lastMessage?.text,
                attachment = lastMessage?.attachment,
                chatTime = time,
                status = status,
                unreadCount = chat.messages.filter { !it.isRead && it.userId != userId }.size
            )
        }

        private fun buildItemDate(updatedAt: String): String {
            val currentDate = System.currentTimeMillis()
            val updateAtDate = getTimeInMillis(updatedAt)

            val currentYear = getFormattedDate(currentDate, "yyyy")
            val updateAtYear = getFormattedDate(updateAtDate, "yyyy")

            val currentMonth = getFormattedDate(currentDate, "M")
            val updateAtMonth = getFormattedDate(updateAtDate, "M")

            val currentDay = getFormattedDate(currentDate, "dd")
            val updateAtDay = getFormattedDate(updateAtDate, "dd")

            return when {
                currentYear.toDouble() > updateAtYear.toDouble() ->
                    getFormattedDate(updateAtDate, "dd.MM.yy")

                currentMonth.toDouble() > updateAtMonth.toDouble() ->
                    getFormattedDate(updateAtDate, "dd MMM")

                currentDay != updateAtDay ->
                    getFormattedDate(updateAtDate, "dd MMM")

                else ->
                    getFormattedDate(updateAtDate, "HH:mm")
            }
        }
    }
}