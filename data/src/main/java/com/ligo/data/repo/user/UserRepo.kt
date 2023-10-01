package com.ligo.data.repo.user

import com.ligo.data.api.UserApi
import com.ligo.data.model.AvatarPhoto
import com.ligo.data.model.Email
import com.ligo.data.model.FCMToken
import com.ligo.data.model.Location
import com.ligo.data.model.LocationRequest
import com.ligo.data.model.Parcel
import com.ligo.data.model.PassportPhoto
import com.ligo.data.model.Trip
import com.ligo.data.model.User
import com.ligo.data.model.UserRequest
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.BaseRepo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class UserRepo(
    private val appPreferences: IAppPreferences,
    private val userApi: UserApi,
) : BaseRepo(), IUserRepo {

    private fun getAuthToken(): String {
        return appPreferences.getUser()?.authToken.orEmpty()
    }

    override fun getUserById(userId: String): Single<User> =
        userApi.getUserById(userId, getAuthToken())
            .proceedWithApiThrowable()

    override fun getDriverTrips(userId: String): Single<List<Trip>> =
        userApi.getDriverTrips(userId, getAuthToken())
            .proceedWithApiThrowable()

    override fun getSenderParcels(userId: String): Single<List<Parcel>> =
        userApi.getSenderParcels(userId, getAuthToken())
            .proceedWithApiThrowable()

    override fun createUser(user: UserRequest): Single<User> =
        userApi.createUser(user)
            .proceedWithApiThrowable()

    override fun updateFcmToken(userId: String, fcmToken: String?): Completable =
        userApi.updateFcmToken(userId, getAuthToken(), FCMToken(fcmToken))
            .proceedWithApiThrowable()

    override fun updateLocation(userId: String, point: Location): Completable =
        userApi.updateLocation(userId, getAuthToken(), LocationRequest(point))
            .proceedWithApiThrowable()

    override fun updatePassportPhoto(userId: String, passportPhoto: String): Completable =
        userApi.updatePassportPhotoUrl(userId, getAuthToken(), PassportPhoto(passportPhoto))
            .proceedWithApiThrowable()

    override fun updateAvatarPhoto(userId: String, avatarPhoto: String): Completable =
        userApi.updateAvatarPhoto(userId, getAuthToken(), AvatarPhoto(avatarPhoto))
            .proceedWithApiThrowable()

    override fun updateUserRating(userId: String, rating: Int): Completable {
        return userApi.updateUserRating(userId, rating, getAuthToken())
            .proceedWithApiThrowable()
    }

    override fun checkUserExists(email: String): Single<Boolean> {
        return userApi.checkUserExists(Email(email))
            .map { it.userExists }
            .proceedWithApiThrowable()
    }
}