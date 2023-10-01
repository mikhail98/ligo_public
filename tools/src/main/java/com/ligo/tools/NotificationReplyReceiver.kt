package com.ligo.tools

import android.app.NotificationManager
import android.app.RemoteInput
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.data.model.MessageRequest
import org.koin.java.KoinJavaComponent.inject

class NotificationReplyReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REPLY = "ACTION_REPLY"

        const val EXTRAS_REPLY_TEXT = "replay_text"
        const val EXTRAS_CHAT_ID = "NotificationReplyReceiver.EXTRAS_CHAT_ID"
        const val EXTRAS_NOTIFICATION_ID = "NotificationReplyReceiver.EXTRAS_NOTIFICATION_ID"
    }

    private val chatsCoordinator: IChatsCoordinator by inject(IChatsCoordinator::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REPLY) {
            val notificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = intent.getIntExtra(EXTRAS_NOTIFICATION_ID, 0)
            val chatId = intent.getStringExtra(EXTRAS_CHAT_ID)
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput != null && chatId != null) {
                val replyText = remoteInput.getCharSequence(EXTRAS_REPLY_TEXT).toString()
                val messageRequest = MessageRequest(text = replyText)
                chatsCoordinator.sendMessage(chatId, messageRequest)
                chatsCoordinator.readMessages(chatId)
                notificationManager.cancel(notificationId)
            }
        }
    }
}