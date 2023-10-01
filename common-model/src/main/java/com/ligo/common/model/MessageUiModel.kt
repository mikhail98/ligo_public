package com.ligo.common.model

import android.view.View
import com.ligo.core.getFormattedDate
import com.ligo.core.getTimeInMillis
import com.ligo.data.MessageReadStatus
import com.ligo.data.model.Attachment
import com.ligo.data.model.Message
import java.util.UUID

sealed class BaseChatMessageUiModel {

    open val view: View? = null
}

data class ChatMessageDateUiModel(val createdAt: String) : BaseChatMessageUiModel()

data class OutChatMessageUiModel(
    val time: String,
    val text: String?,
    val attachment: Attachment?,
    val status: MessageReadStatus,
    override val view: View? = null,
) : BaseChatMessageUiModel() {
    companion object {
        fun fromMessage(message: Message): OutChatMessageUiModel? {
            val text = message.text
            val attachment = message.attachment
            if (text == null && attachment == null) return null

            val isLocalMessage = try {
                UUID.fromString(message.id)
                true
            } catch (e: Throwable) {
                false
            }

            val status = when {
                isLocalMessage -> MessageReadStatus.SENDING
                message.isRead -> MessageReadStatus.READ
                else -> MessageReadStatus.SENT
            }

            val timeMillis = if (status == MessageReadStatus.SENDING) {
                System.currentTimeMillis()
            } else {
                getTimeInMillis(message.createdAt)
            }

            return OutChatMessageUiModel(
                text = text,
                attachment = attachment,
                time = getFormattedDate(timeMillis, dateFormat = "HH:mm"),
                status = status
            )
        }
    }
}

data class InChatMessageUiModel(
    val time: String,
    val text: String?,
    val attachment: Attachment?,
    override val view: View? = null,
) : BaseChatMessageUiModel() {
    companion object {
        fun fromMessage(message: Message): InChatMessageUiModel? {
            val text = message.text
            val attachment = message.attachment
            if (text == null && attachment == null) return null
            return InChatMessageUiModel(
                text = text,
                attachment = attachment,
                time = getFormattedDate(getTimeInMillis(message.createdAt), dateFormat = "HH:mm")
            )
        }
    }
}