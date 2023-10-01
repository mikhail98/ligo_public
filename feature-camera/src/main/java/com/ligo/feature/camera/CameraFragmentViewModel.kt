package com.ligo.feature.camera

import android.net.Uri
import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.IQrManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class CameraFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val qrManager: IQrManager,
    private val photoManager: IPhotoManager,
) : BaseViewModel(navigator, analytics) {

    private val closeSubject: Subject<Unit> =
        PublishSubject.create<Unit>().toSerialized()

    fun photoTaken(uri: Uri, realPath: String) {
        closeSubject.onNext(Unit)
        photoManager.photoTaken(uri, realPath)
    }

    fun qrScanned(data: String) {
        closeSubject.onNext(Unit)
        qrManager.qrScanned(data)
    }

    fun getOnCloseObservable(): Observable<Unit> = closeSubject
}