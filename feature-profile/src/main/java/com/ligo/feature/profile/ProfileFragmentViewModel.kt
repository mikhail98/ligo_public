package com.ligo.feature.profile

import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.core.Initializable
import com.ligo.data.model.TripStatus
import com.ligo.data.model.User
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.auth.IAuthRepo
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.data.repo.user.IUserRepo
import com.ligo.data.socket.ISocketService
import com.ligo.data.socket.event.OutcomingSocketEvent
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IAuthManager
import com.ligo.google.api.IFcmTokenManager
import com.ligo.google.api.IStorageManager
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.toggler.api.Feature
import com.ligo.toggler.api.IToggler
import com.ligo.tools.api.IInitializer
import com.ligo.tools.api.ILocationTracker
import com.ligo.tools.api.INotificationManager
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.PickPhoto
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.io.File

class ProfileFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val toggler: IToggler,
    private val userRepo: IUserRepo,
    private val authRepo: IAuthRepo,
    private val photoManager: IPhotoManager,
    private val googleRepo: IGoogleApisRepo,
    private val socketService: ISocketService,
    private val googleAuthManager: IAuthManager,
    private val appPreferences: IAppPreferences,
    private val storageManager: IStorageManager,
    private val locationTracker: ILocationTracker,
    private val fcmTokenManager: IFcmTokenManager,
    private val chatsCoordinator: IChatsCoordinator,
    private val notificationManager: INotificationManager,
    private val initializer: IInitializer,
) : BaseViewModel(navigator, analytics) {

    private val userSubject: Subject<User> = PublishSubject.create<User>().toSerialized()
    private val avatarLoadingSubject: Subject<Pair<Boolean, String?>> =
        PublishSubject.create<Pair<Boolean, String?>>().toSerialized()
    private val deleteAccountRequestResult: Subject<DeleteAccountRequestResult> =
        PublishSubject.create<DeleteAccountRequestResult>().toSerialized()

    override fun onCreate() {
        photoManager.getOnPhotoPickedObservable().subscribeAndDispose(::uploadPhoto)
    }

    fun fetchUser() {
        appPreferences.getUser()?.apply {
            userSubject.onNext(this)
        }
    }

    fun logout() {
        val user = appPreferences.getUser() ?: return
        setLoading(true)
        locationTracker.stopLocationTracking()
        val payload = OutcomingSocketEvent.LeaveSocket.Payload(user._id)
        socketService.emitEvent(OutcomingSocketEvent.LeaveSocket(payload))
        fcmTokenManager.fetchToken()
            .flatMapCompletable { authRepo.logout(user._id, it) }
            .subscribeAndDispose { handleSignedOut() }
    }

    fun requestDeleteAccount() {
        setLoading(true)
        userRepo.getDriverTrips(appPreferences.getUser()?._id ?: return)
            .subscribeOn(Schedulers.io())
            .map { trips -> trips.any { it.status == TripStatus.ACTIVE } }
            .subscribeAndDispose(::handleHasActiveTrips)
    }

    private fun handleHasActiveTrips(hasActiveTrips: Boolean) {
        setLoading(false)
        val result = if (hasActiveTrips) {
            DeleteAccountRequestResult.NOT_ALLOWED_DRIVER_HAS_ACTIVE_TRIPS
        } else {
            DeleteAccountRequestResult.ALLOWED
        }
        deleteAccountRequestResult.onNext(result)
    }

    fun deleteAccount() {
        val user = appPreferences.getUser() ?: return
        setLoading(true)

        locationTracker.stopLocationTracking()
        val payload = OutcomingSocketEvent.LeaveSocket.Payload(user._id)
        socketService.emitEvent(OutcomingSocketEvent.LeaveSocket(payload))

        authRepo.deleteAccount(user._id).subscribeAndDispose { handleSignedOut() }
    }

    private fun handleSignedOut() {
        appPreferences.logout()
        initializer.on(Initializable.On.LOGOUT)
        googleAuthManager.signOut()
        navigator.open(Target.Splash)
    }

    fun takeAvatar() {
        photoManager.takePhoto(
            PickPhoto.Source.CAMERA,
            PickPhoto.Origin.AVATAR,
            PickPhoto.Type.AVATAR
        )
    }

    private fun uploadPhoto(pickedPhoto: PickPhoto) {
        if (pickedPhoto.origin != PickPhoto.Origin.AVATAR) return
        val user = appPreferences.getUser() ?: return
        avatarLoadingSubject.onNext(true to user.avatarPhoto)
        storageManager.uploadMedia(pickedPhoto.uri, IStorageManager.FileType.PHOTO, user.email)
            .subscribeAndDispose { avatarPhotoUrl ->
                pickedPhoto.realPath?.apply { File(this).delete() }
                userRepo.updateAvatarPhoto(user._id, avatarPhotoUrl)
                    .subscribeAndDispose {
                        appPreferences.saveUser(user.copy(avatarPhoto = avatarPhotoUrl))
                        avatarLoadingSubject.onNext(false to avatarPhotoUrl)
                    }
            }
    }

    fun getUserObservable(): Observable<User> = userSubject

    fun getAvatarLoadingObservable(): Observable<Pair<Boolean, String?>> = avatarLoadingSubject

    fun getDeleteAccountRequestResultObservable(): Observable<DeleteAccountRequestResult> =
        deleteAccountRequestResult

    fun getChatAvailableObservable(): Observable<Boolean> =
        toggler.getAvailableFeatureListObservable()
            .map { it.contains(Feature.CHAT) }

    fun getUnreadChatCountObservable(): Observable<Int> =
        Observable.combineLatest(
            chatsCoordinator.getUnreadChatCountObservable(),
            getChatAvailableObservable()
        ) { unreadCount, chatAvailable ->
            if (chatAvailable) unreadCount else 0
        }

    fun parseLocalization() {
        googleRepo.parseLocalization().subscribeAndDispose { }
    }

    fun sendTestPush() {
        val channelId = INotificationManager.NOTIFICATION_CHANNEL_ID
        val data =
            "{\"key\":\"PARCEL_AVAILABLE\",\"parcelId\":\"65089e30b57c0594b62e54b6\",\"route\":\"test\",\"senderId\":\"64eefe3befea4e1d79fbfe30\"}"
        notificationManager.sendNotification(channelId, data)
    }
}