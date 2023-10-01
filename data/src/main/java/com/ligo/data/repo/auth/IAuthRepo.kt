package com.ligo.data.repo.auth

import com.ligo.data.model.LoginUser
import com.ligo.data.model.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IAuthRepo {

    fun login(user: LoginUser): Single<User>

    fun logout(userId: String, fcmToken: String?): Completable

    fun deleteAccount(userId: String): Completable
}