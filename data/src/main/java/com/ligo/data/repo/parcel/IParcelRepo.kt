package com.ligo.data.repo.parcel

import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRejectRequest
import com.ligo.data.model.ParcelRequest
import com.ligo.data.model.Secret
import io.reactivex.rxjava3.core.Single

interface IParcelRepo {

    fun createParcel(parcel: ParcelRequest): Single<Parcel>

    fun getParcelById(parcelId: String): Single<Parcel>

    fun pickupParcel(parcelId: String): Single<Parcel>

    fun rejectParcel(parcelId: String, rejectRequest: ParcelRejectRequest): Single<Parcel>

    fun acceptParcel(parcelId: String): Single<Parcel>

    fun declineParcel(parcelId: String): Single<Parcel>

    fun cancelParcel(parcelId: String): Single<Parcel>

    fun deliverParcel(parcelId: String, secret: String): Single<Parcel>

    fun createSecret(parcelId: String, secret: String): Single<Secret>

    fun getSecret(parcelId: String): Single<Secret>
}