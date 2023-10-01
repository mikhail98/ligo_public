package com.ligo.subfeature.createparcel

import android.net.Uri
import com.ligo.common.BaseViewModel
import com.ligo.core.printError
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRequest
import com.ligo.data.model.Price
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IRemoteConfig
import com.ligo.google.api.IStorageManager
import com.ligo.google.api.RemoteConfigCurrency
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.subfeature.createparcel.parceldetails.ParcelDetails
import com.ligo.tools.api.IPlaceSearchManager
import com.ligo.tools.api.SearchPlaceRequest
import com.ligo.tools.api.SearchPlaceResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Optional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class CreateParcelFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    remoteConfig: IRemoteConfig,
    private val storageManager: IStorageManager,
    private val appPreferences: IAppPreferences,
    private val searchManager: IPlaceSearchManager,
    private val parcelsCoordinator: ISenderParcelsCoordinator,
) : BaseViewModel(navigator, analytics) {

    private val defaultCurrency =
        remoteConfig.getString(IRemoteConfig.DEFAULT_CURRENCY)

    private val createParcelDataSubject: Subject<CreateParcelData> =
        PublishSubject.create<CreateParcelData>().toSerialized()

    val createParcelData = CreateParcelData(currencyCode = defaultCurrency)

    override fun onCreate() {
        searchManager.getOnPlacePickedObservable()
            .subscribe(::setPoint, ::printError)
            .also(::addDisposable)
    }

    fun createParcel() {
        val userEmail = appPreferences.getUser()?.email ?: return
        val request = createParcelRequest() ?: return
        setLoading(true)

        storageManager.uploadMedia(
            Uri.parse(request.parcelPhoto),
            IStorageManager.FileType.PHOTO,
            userEmail
        )
            .map { request.copy(parcelPhoto = it) }
            .flatMap { parcelsCoordinator.createParcel(it).subscribeOn(Schedulers.io()) }
            .subscribeAndDispose(::handleParcelCreated)
    }

    private fun handleParcelCreated(parcel: Parcel) {
        setLoading(false)
        reset()
        navigator.open(Target.SearchForDriver(parcel._id))
    }

    private fun createParcelRequest(): ParcelRequest? {
        val userId = appPreferences.getUser()?._id ?: return null

        val parcelPhotoUrl = createParcelData.parcelPhotoUrl ?: return null
        val startPoint = createParcelData.startPoint ?: return null
        val endPoint = createParcelData.endPoint ?: return null
        val types = createParcelData.types
        val reward = createParcelData.reward
        val weight = createParcelData.weight

        val selectedCurrency = RemoteConfigCurrency.fromCode(createParcelData.currencyCode)
        return if (types.isNotEmpty() && reward > 0 && weight > 0) {
            ParcelRequest(
                userId = userId,
                startPoint = startPoint,
                endPoint = endPoint,
                types = types,
                price = Price(value = reward, currency = selectedCurrency.code),
                weight = weight,
                secret = UUID.randomUUID().toString(),
                parcelPhoto = parcelPhotoUrl
            )
        } else {
            null
        }
    }

    fun reset() {
        createParcelData.reset(defaultCurrency)
        createParcelDataSubject.onNext(createParcelData)
    }

    private fun setPoint(result: SearchPlaceResult) {
        val location = result.location
        when (result.origin) {
            SearchPlaceRequest.Origin.SEND_PARCEL_FROM -> {
                createParcelData.startPoint = location
                createParcelDataSubject.onNext(createParcelData)
            }

            SearchPlaceRequest.Origin.SEND_PARCEL_TO -> {
                createParcelData.endPoint = location
                createParcelDataSubject.onNext(createParcelData)
            }

            else -> Unit
        }
    }

    fun setCurrencyCode(currencyCode: String) {
        createParcelData.currencyCode = currencyCode
        createParcelDataSubject.onNext(createParcelData)
    }

    fun setReward(text: String) {
        val reward = try {
            text.toInt()
        } catch (ex: NumberFormatException) {
            -1
        }
        createParcelData.reward = reward
        createParcelDataSubject.onNext(createParcelData)
    }

    fun setParcelDetails(parcelDetailsOptional: Optional<ParcelDetails>) {
        val parcelDetails = parcelDetailsOptional.getOrNull() ?: return
        setParcelPhotoUrl(parcelDetails.parcelPhotoUrl)
        setTypeList(parcelDetails.typeList)
        setWeight(parcelDetails.weight)
    }

    private fun setTypeList(types: List<String>) {
        createParcelData.types = types
        createParcelDataSubject.onNext(createParcelData)
    }

    private fun setWeight(weight: Int) {
        createParcelData.weight = weight
        createParcelDataSubject.onNext(createParcelData)
    }

    private fun setParcelPhotoUrl(photoUrl: String?) {
        createParcelData.parcelPhotoUrl = photoUrl
        createParcelDataSubject.onNext(createParcelData)
    }

    fun getCreateParcelDataObservable(): Observable<CreateParcelData> = createParcelDataSubject
}