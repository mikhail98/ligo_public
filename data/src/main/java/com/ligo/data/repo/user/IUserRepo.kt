package com.ligo.data.repo.user

import com.ligo.data.model.Location
import com.ligo.data.model.Parcel
import com.ligo.data.model.Trip
import com.ligo.data.model.User
import com.ligo.data.model.UserRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IUserRepo {

    fun getUserById(userId: String): Single<User>

    fun getDriverTrips(userId: String): Single<List<Trip>>

    fun getSenderParcels(userId: String): Single<List<Parcel>>

    fun createUser(user: UserRequest): Single<User>

    fun updateFcmToken(userId: String, fcmToken: String?): Completable

    fun updateLocation(userId: String, point: Location): Completable

    fun updatePassportPhoto(userId: String, passportPhoto: String): Completable

    fun updateAvatarPhoto(userId: String, avatarPhoto: String): Completable

    fun updateUserRating(userId: String, rating: Int): Completable

    fun checkUserExists(email: String): Single<Boolean>
}