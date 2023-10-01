package com.ligo.data.repo.auth

import com.ligo.data.api.AuthApi
import com.ligo.data.model.FCMToken
import com.ligo.data.model.LoginUser
import com.ligo.data.model.User
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.BaseRepo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class AuthRepo(
    private val appPreferences: IAppPreferences,
    private val authApi: AuthApi,
) : BaseRepo(), IAuthRepo {

    private fun getAuthToken(): String {
        return appPreferences.getUser()?.authToken.orEmpty()
    }

    override fun login(user: LoginUser): Single<User> =
        authApi.login(user)
            .proceedWithApiThrowable()

    override fun logout(userId: String, fcmToken: String?): Completable =
        authApi.logout(userId, FCMToken(fcmToken), getAuthToken())
            .proceedWithApiThrowable()

    override fun deleteAccount(userId: String): Completable =
        authApi.deleteAccount(userId, getAuthToken())
            .proceedWithApiThrowable()
}