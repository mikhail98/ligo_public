package com.ligo.tools.api

import android.app.Notification

interface INotificationManager {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "ligo_demo_notification_channel"
    }

    fun sendNotification(channelId: String, data: String?)

    fun createNotificationChannel()

    fun getLocationTrackerNotification(): Notification
}
