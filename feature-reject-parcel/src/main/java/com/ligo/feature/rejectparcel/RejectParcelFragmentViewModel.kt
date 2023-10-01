package com.ligo.feature.rejectparcel

import com.ligo.common.BaseViewModel
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.model.ParcelRejectReason
import com.ligo.data.model.ParcelRejectRequest
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IStorageManager
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.PickPhoto
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.io.File

class RejectParcelFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val photoManager: IPhotoManager,
    private val appPreferences: IAppPreferences,
    private val storageManager: IStorageManager,
    private val tripsCoordinator: IDriverTripCoordinator,
) : BaseViewModel(navigator, analytics) {

    private var rejectPhotoUrl: String? = null

    private val photoUploadingSubject: Subject<Pair<PickPhoto?, Boolean>> =
        PublishSubject.create<Pair<PickPhoto?, Boolean>>().toSerialized()

    override fun onCreate() {
        photoManager.getOnPhotoPickedObservable().subscribeAndDispose(::handlePhotoPicked)
    }

    fun takePhotoForParcelRejection() {
        photoManager.takePhoto(
            PickPhoto.Source.CAMERA,
            PickPhoto.Origin.PARCEL_REJECTION,
            PickPhoto.Type.REGULAR_PHOTO
        )
    }

    private fun handlePhotoPicked(pickedPhoto: PickPhoto) {
        val user = appPreferences.getUser() ?: return
        photoUploadingSubject.onNext(pickedPhoto to true)
        storageManager.uploadMedia(pickedPhoto.uri, IStorageManager.FileType.PHOTO, user.email)
            .subscribeAndDispose { url ->
                rejectPhotoUrl = url
                pickedPhoto.realPath?.apply { File(this).delete() }
                if (pickedPhoto.origin == PickPhoto.Origin.PARCEL_REJECTION) {
                    photoUploadingSubject.onNext(pickedPhoto to false)
                }
            }
    }

    fun removePickedPhoto() {
        rejectPhotoUrl = null
        photoUploadingSubject.onNext(null to false)
    }

    fun rejectParcel(parcelId: String, reason: ParcelRejectReason, comment: String? = null) {
        setLoading(true)
        val rejectData = ParcelRejectRequest(reason, comment, rejectPhotoUrl)
        tripsCoordinator.rejectParcel(parcelId, rejectData)
            .subscribeAndDispose { navigator.close(Target.RejectParcel::class.java) }
    }

    fun getPhotoUploadingObservable(): Observable<Pair<PickPhoto?, Boolean>> =
        photoUploadingSubject
}