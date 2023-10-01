package com.ligo.tools

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.Gson
import com.ligo.core.R
import com.ligo.core.loadRoundImageWithGlide
import com.ligo.core.printError
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.LOOKING_FOR_A_TRIP
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_ACCEPTED_CONTENT
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_ACCEPTED_TITLE
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_AVAILABLE
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_DELIVERED_CONTENT
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_DELIVERED_TITLE
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_PICKED_CONTENT
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_PICKED_TITLE
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_REJECTED_CONTENT
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_PARCEL_REJECTED_TITLE
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_ROUTE_PREFIX
import com.ligo.data.model.ConfigStringKey.NOTIFICATION_START_TRIP_REMAINDER_TITLE
import com.ligo.data.model.ConfigStringKey.REPLY
import com.ligo.data.model.Message
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.PushArgs
import com.ligo.navigator.api.Target
import com.ligo.navigator.api.Target.Chat
import com.ligo.toggler.api.Feature
import com.ligo.toggler.api.IToggler
import com.ligo.tools.NotificationReplyReceiver.Companion.ACTION_REPLY
import com.ligo.tools.NotificationReplyReceiver.Companion.EXTRAS_CHAT_ID
import com.ligo.tools.NotificationReplyReceiver.Companion.EXTRAS_NOTIFICATION_ID
import com.ligo.tools.NotificationReplyReceiver.Companion.EXTRAS_REPLY_TEXT
import com.ligo.tools.api.ILocalizationManager
import com.ligo.tools.api.INotificationManager
import com.ligo.tools.api.INotificationManager.Companion.NOTIFICATION_CHANNEL_ID
import com.ligo.tools.api.PushData
import io.reactivex.rxjava3.disposables.Disposable

internal class NotificationManager(
    private val context: Context,
    private val toggler: IToggler,
    private val navigator: INavigator,
    private val appPreferences: IAppPreferences,
    private val localizationManager: ILocalizationManager,
) : INotificationManager, LifecycleEventObserver {

    companion object {
        private const val NOTIFICATION_CHANNEL_NAME = "General"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION = "All notifications"

        private const val KEY_PARCEL_AVAILABLE = "PARCEL_AVAILABLE"
        private const val KEY_PARCEL_ACCEPTED = "PARCEL_ACCEPTED"
        private const val KEY_PARCEL_REJECTED = "PARCEL_REJECTED"
        private const val KEY_PARCEL_PICKED = "PARCEL_PICKED"
        private const val KEY_PARCEL_DELIVERED = "PARCEL_DELIVERED"
        private const val KEY_START_TRIP_REMINDER = "START_TRIP_REMINDER"
        private const val KEY_CHAT_MESSAGE = "NEW_MESSAGE"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private var isInForeground = false

    private val disposableMap: MutableMap<String, Disposable> = mutableMapOf()

    init {
        handler.post { ProcessLifecycleOwner.get().lifecycle.addObserver(this) }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> isInForeground = true
            Lifecycle.Event.ON_PAUSE -> isInForeground = false
            else -> Unit
        }
    }

    override fun sendNotification(channelId: String, data: String?) {
        val pushData = Gson().fromJson(data, PushData::class.java)
        when (pushData.key) {
            KEY_PARCEL_AVAILABLE -> handleParcelAvailable(
                pushData.parcelId ?: return,
                pushData.senderId ?: return,
                pushData.route ?: return
            )

            KEY_PARCEL_ACCEPTED -> handleParcelAccepted(
                pushData.parcelId ?: return
            )

            KEY_PARCEL_REJECTED -> handleParcelRejected(
                pushData.parcelId ?: return
            )

            KEY_PARCEL_PICKED -> handleParcelPicked(
                pushData.parcelId ?: return
            )

            KEY_PARCEL_DELIVERED -> handleParcelDelivered(
                pushData.parcelId ?: return
            )

            KEY_START_TRIP_REMINDER -> handleStartTripReminder(
                pushData.tripId ?: return,
                pushData.route ?: return
            )

            KEY_CHAT_MESSAGE -> handleMessage(
                pushData.message ?: return,
                pushData.chat ?: return
            )
        }
    }

    private fun handleMessage(message: Message, chat: PushData.Chat) {
        toggler.getAvailableFeatureListObservable()
            .subscribe(
                { if (it.contains(Feature.CHAT)) showMessagePush(message, chat) },
                ::printError
            )
            .also { disposableMap[message.id] = it }
    }

    private fun showMessagePush(message: Message, chat: PushData.Chat) {
        var needToShowPush = true

        val topLevelFeature = navigator.topLevelFeature
        if (topLevelFeature is Chat) {
            if (topLevelFeature.chatId == chat.id && isInForeground) {
                needToShowPush = false
            }
        }

        if (!needToShowPush) return

        val isDriver = appPreferences.getUser()?._id == chat.driverId

        val contentTitle = if (isDriver) chat.senderName else chat.driverName
        val largeIconUrl =
            message.attachment?.mediaUrl ?: if (isDriver) chat.senderAvatar else chat.driverAvatar
        val contentText = if (message.text.isNullOrEmpty()) {
            localizationManager.getLocalized(ConfigStringKey.NEW_MESSAGE)
        } else {
            message.text.orEmpty()
        }
        val notificationId = chat.id.hashCode()

        val bigText = notificationManager.activeNotifications.find {
            it.id == notificationId
        }?.let {
            val currentText =
                it.notification.extras.getString(Notification.EXTRA_BIG_TEXT) ?: contentText
            currentText.plus("\n").plus(message.text)
        } ?: contentText

        val unreadCount = bigText.filter { it == '\n' }.length
        val subText = if (unreadCount > 0) "${unreadCount + 1}" else null

        val replyIntent = Intent(context, NotificationReplyReceiver::class.java)
            .apply {
                action = ACTION_REPLY
                data = Uri.parse(chat.id)
                putExtra(EXTRAS_NOTIFICATION_ID, notificationId)
                putExtra(EXTRAS_CHAT_ID, chat.id)
            }

        val replyPendingIntent =
            if (VERSION.SDK_INT >= VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context,
                    0,
                    replyIntent,
                    FLAG_MUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    context,
                    0,
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        val replyText = localizationManager.getLocalized(REPLY)
        val remoteInput = RemoteInput.Builder(EXTRAS_REPLY_TEXT)
            .setLabel(replyText)
            .build()

        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_telegram,
            replyText,
            replyPendingIntent
        )
            .addRemoteInput(remoteInput).build()

        createAndShowPush(
            notificationId = notificationId,
            contentTitle = contentTitle,
            contentText = contentText,
            contentIntent = getContentIntent(
                PushArgs(
                    homeTarget = PushArgs.HomeTarget.PROFILE,
                    chatId = chat.id
                )
            ),
            largeIconUrl = largeIconUrl,
            bigText = bigText,
            subText = subText,
            action = action
        )
    }

    private fun handleStartTripReminder(tripId: String, route: String) {
        createAndShowPush(
            tripId.hashCode(),
            localizationManager.getLocalized(NOTIFICATION_START_TRIP_REMAINDER_TITLE),
            localizationManager.getLocalized(NOTIFICATION_ROUTE_PREFIX) + route,
            getContentIntent()
        )
    }

    private fun handleParcelAvailable(parcelId: String, senderId: String, route: String) {
        if (senderId == appPreferences.getUser()?._id) return

        var needToShowPush = true

        val topLevelFeature = navigator.topLevelFeature
        if (topLevelFeature is Target.DriverTrip && isInForeground) {
            needToShowPush = false
        }

        if (!needToShowPush) return

        createAndShowPush(
            parcelId.hashCode(),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_AVAILABLE),
            localizationManager.getLocalized(NOTIFICATION_ROUTE_PREFIX) + route,
            getContentIntent(PushArgs(availableParcelId = parcelId))
        )
    }

    private fun handleParcelRejected(parcelId: String) {
        createAndShowPush(
            parcelId.hashCode(),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_REJECTED_TITLE),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_REJECTED_CONTENT),
            getContentIntent(PushArgs(homeTarget = PushArgs.HomeTarget.HISTORY))
        )
    }

    private fun handleParcelAccepted(parcelId: String) {
        createAndShowPush(
            parcelId.hashCode(),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_ACCEPTED_TITLE),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_ACCEPTED_CONTENT),
            getContentIntent(PushArgs(homeTarget = PushArgs.HomeTarget.HISTORY))
        )
    }

    private fun handleParcelPicked(parcelId: String) {
        if (isInForeground) return

        createAndShowPush(
            parcelId.hashCode(),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_PICKED_TITLE),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_PICKED_CONTENT),
            getContentIntent(PushArgs(homeTarget = PushArgs.HomeTarget.HISTORY))
        )
    }

    private fun handleParcelDelivered(parcelId: String) {
        createAndShowPush(
            parcelId.hashCode(),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_DELIVERED_TITLE),
            localizationManager.getLocalized(NOTIFICATION_PARCEL_DELIVERED_CONTENT),
            getContentIntent(PushArgs(homeTarget = PushArgs.HomeTarget.HISTORY))
        )
    }

    private fun getContentIntent(pushArgs: PushArgs = PushArgs()): PendingIntent {
        val notificationIntent = navigator.provideMainAppIntent(context)
        notificationIntent.putExtra(INavigator.EXTRA_PUSH_ARGS, pushArgs)
        return PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 1000).toInt(),
            notificationIntent,
            FLAG_MUTABLE
        )
    }

    override fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
        notificationChannel.setShowBadge(true)
        notificationChannel.enableVibration(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createAndShowPush(
        notificationId: Int,
        contentTitle: String,
        contentText: String,
        contentIntent: PendingIntent? = null,
        largeIconUrl: String? = null,
        bigText: String? = null,
        subText: String? = null,
        action: NotificationCompat.Action? = null,
    ) {
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setAutoCancel(true)
            .setSubText(subText)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(Notification.DEFAULT_ALL)
            .addAction(action)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (contentIntent != null) {
            notificationBuilder.setContentIntent(contentIntent)
        }

        if (largeIconUrl != null) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            context.loadRoundImageWithGlide(largeIconUrl) {
                notificationBuilder.setLargeIcon(it)
                notificationManager.notify(notificationId, notificationBuilder.build())
            }
        } else {
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    override fun getLocationTrackerNotification(): Notification {
        val notificationText = localizationManager.getLocalized(LOOKING_FOR_A_TRIP)
        val titleText = context.getText(R.string.app_name)

        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(notificationText)
            .setBigContentTitle(titleText)

        val notificationIntent = navigator.provideMainAppIntent(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 1000).toInt(),
            notificationIntent,
            FLAG_MUTABLE
        )

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
}
