package com.ligo.google

import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ligo.core.PermissionChecker
import com.ligo.core.printError
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.user.IUserRepo
import com.ligo.tools.api.INotificationManager
import com.ligo.tools.api.INotificationManager.Companion.NOTIFICATION_CHANNEL_ID
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject

class MessagingService : FirebaseMessagingService() {

    private val notificationsManager by inject<INotificationManager>()
    private val appPreferences by inject<IAppPreferences>()
    private val userRepo by inject<IUserRepo>()

    private val createdCompositeDisposable = CompositeDisposable()

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        val user = appPreferences.getUser() ?: return
        userRepo.updateFcmToken(user._id, newToken)
            .subscribeOn(Schedulers.io())
            .subscribe({}, ::printError)
            .also(createdCompositeDisposable::add)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data["data"]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PermissionChecker.isNotificationPermissionEnabled(applicationContext).first) {
                notificationsManager.sendNotification(NOTIFICATION_CHANNEL_ID, data)
            }
        } else {
            notificationsManager.sendNotification(NOTIFICATION_CHANNEL_ID, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        createdCompositeDisposable.clear()
    }
}