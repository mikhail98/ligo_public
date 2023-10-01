package com.ligo.data.repo

import com.google.gson.Gson
import com.ligo.core.printError
import com.ligo.data.model.ApiThrowable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException

internal abstract class BaseRepo {
    protected fun <T : Any> Single<T>.proceedWithApiThrowable(): Single<T> {
        return onErrorResumeNext {
            if (it is HttpException) {
                Single.error(mapError(it))
            } else {
                Single.error(it)
            }
        }
    }

    protected fun <T : Any> Observable<T>.proceedWithApiThrowable(): Observable<T> {
        return onErrorResumeNext {
            if (it is HttpException) {
                Observable.error(mapError(it))
            } else {
                Observable.error(it)
            }
        }
    }

    protected fun Completable.proceedWithApiThrowable(): Completable {
        return onErrorResumeNext {
            if (it is HttpException) {
                Completable.error(mapError(it))
            } else {
                Completable.error(it)
            }
        }
    }

    private fun mapError(httpException: HttpException): Throwable {
        return try {
            val message = httpException.response()?.errorBody()?.string().orEmpty()
            val error = Gson().fromJson(message, ApiThrowable::class.java)
            error.mapToLocalError()
        } catch (e: Exception) {
            printError(e)
            httpException
        }
    }
}