package com.ligo.data.repo.parcel

import com.ligo.data.api.ParcelApi
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRejectRequest
import com.ligo.data.model.ParcelRequest
import com.ligo.data.model.Secret
import com.ligo.data.model.SecretPayload
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.BaseRepo
import io.reactivex.rxjava3.core.Single

internal class ParcelRepo(
    private val appPreferences: IAppPreferences,
    private val parcelApi: ParcelApi,
) : BaseRepo(), IParcelRepo {

    private fun getAuthToken(): String {
        return appPreferences.getUser()?.authToken.orEmpty()
    }

    override fun createParcel(parcel: ParcelRequest): Single<Parcel> {
        return parcelApi.createParcel(getAuthToken(), parcel)
            .proceedWithApiThrowable()
    }

    override fun getParcelById(parcelId: String): Single<Parcel> {
        return parcelApi.getParcelById(getAuthToken(), parcelId)
            .proceedWithApiThrowable()
    }

    override fun pickupParcel(parcelId: String): Single<Parcel> {
        return parcelApi.pickupParcel(getAuthToken(), parcelId)
            .proceedWithApiThrowable()
    }

    override fun rejectParcel(
        parcelId: String,
        rejectRequest: ParcelRejectRequest,
    ): Single<Parcel> {
        return parcelApi.rejectParcel(getAuthToken(), parcelId, rejectRequest)
            .proceedWithApiThrowable()
    }

    override fun cancelParcel(parcelId: String): Single<Parcel> {
        return parcelApi.cancelParcel(getAuthToken(), parcelId)
            .proceedWithApiThrowable()
    }

    override fun acceptParcel(parcelId: String): Single<Parcel> {
        return parcelApi.acceptParcel(getAuthToken(), parcelId)
            .proceedWithApiThrowable()
    }

    override fun declineParcel(parcelId: String): Single<Parcel> {
        return parcelApi.declineParcel(getAuthToken(), parcelId)
            .proceedWithApiThrowable()
    }

    override fun deliverParcel(parcelId: String, secret: String): Single<Parcel> {
        return parcelApi.deliverParcel(getAuthToken(), parcelId, SecretPayload(secret))
            .proceedWithApiThrowable()
    }

    override fun createSecret(parcelId: String, secret: String): Single<Secret> {
        return parcelApi.createSecret(getAuthToken(), parcelId, SecretPayload(secret))
            .proceedWithApiThrowable()
    }

    override fun getSecret(parcelId: String): Single<Secret> {
        return parcelApi.getSecret(getAuthToken(), parcelId)
            .proceedWithApiThrowable()
    }
}