package com.ligo.subfeature.createparcel.parceldetails

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.PickPhoto
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class ParcelDetailsBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val photoManager: IPhotoManager,
) : BaseViewModel(navigator, analytics) {

    private val parcelDetailsSubject: Subject<ParcelDetails> =
        PublishSubject.create<ParcelDetails>().toSerialized()

    val parcelDetails = ParcelDetails()

    override fun onCreate() {
        super.onCreate()
        photoManager.getOnPhotoPickedObservable().subscribeAndDispose(::handlePhoto)
    }

    private fun handlePhoto(pickPhoto: PickPhoto) {
        if (pickPhoto.origin != PickPhoto.Origin.PARCEL) return
        setPhoto(pickPhoto.uri.toString())
    }

    fun init(parcelDetails: ParcelDetails?) {
        parcelDetails ?: return
        this.parcelDetails.parcelPhotoUrl = parcelDetails.parcelPhotoUrl
        this.parcelDetails.typeList = parcelDetails.typeList
        this.parcelDetails.weight = parcelDetails.weight
        parcelDetailsSubject.onNext(parcelDetails)
    }

    fun setWeight(text: String) {
        val weight = try {
            text.toInt()
        } catch (ex: NumberFormatException) {
            -1
        }
        parcelDetails.weight = weight
        parcelDetailsSubject.onNext(parcelDetails)
    }

    fun setTypeList(types: List<String>) {
        parcelDetails.typeList = types
        parcelDetailsSubject.onNext(parcelDetails)
    }

    fun setPhoto(url: String?) {
        parcelDetails.parcelPhotoUrl = url
        parcelDetailsSubject.onNext(parcelDetails)
    }

    fun pickPhoto(source: PickPhoto.Source) {
        photoManager.takePhoto(source, PickPhoto.Origin.PARCEL)
    }

    fun getParcelDetailsObservable(): Observable<ParcelDetails> {
        return parcelDetailsSubject
    }
}