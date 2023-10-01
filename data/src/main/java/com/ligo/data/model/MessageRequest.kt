package com.ligo.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

class CreateMessageRequest(
    @SerializedName("message")
    val message: MessageRequest,
)

data class MessageRequest(
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("text")
    var text: String? = null,
    @SerializedName("attachment")
    var attachment: Attachment? = null,
) {
    fun toMessage(chatId: String, userId: String): Message {
        return Message(
            id = id,
            text = text,
            attachment = attachment,
            chatId = chatId,
            userId = userId,
            isDeleted = false,
            isEdited = false,
            isRead = false,
            createdAt = ""
        )
    }
}