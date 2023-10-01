package com.ligo.tools

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.ligo.tools.api.INotificationManager
import org.koin.android.ext.android.inject

internal class LocationTrackerService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1627

        fun getIntent(context: Context): Intent {
            return Intent(context, LocationTrackerService::class.java)
        }
    }

    private val notificationsManager by inject<INotificationManager>()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notificationsManager.getLocationTrackerNotification())
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
