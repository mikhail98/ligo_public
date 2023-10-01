package com.ligo.common

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import com.ligo.core.printError
import com.ligo.data.model.ApiThrowable
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

abstract class BaseViewModel(
    val navigator: INavigator,
    protected val analytics: IAnalytics,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val onApiErrorSubject: Subject<ApiThrowable> = PublishSubject.create()

    private val onLoadingSubject: Subject<Boolean> = PublishSubject.create()

    private val onSnackbarSubject: Subject<String> = PublishSubject.create()

    open fun onCreate() {
        // do nothing
    }

    open fun handleOtherErrors(throwable: Throwable) {
        handleError(ApiThrowable.Fatal)
    }

    protected fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    open fun onDestroy() {
        disposables.clear()
    }

    fun setLoading(isLoading: Boolean) {
        onLoadingSubject.onNext(isLoading)
    }

    fun handleError(error: Throwable) {
        if (error is ApiThrowable) {
            onApiErrorSubject.onNext(error)
        } else {
            handleOtherErrors(error)
        }
    }

    protected fun handleSnackbar(configStringKey: String) {
        onSnackbarSubject.onNext(configStringKey)
    }

    fun logEvent(eventName: String, bundle: Bundle = bundleOf()) {
        analytics.logEvent(eventName, bundle)
    }

    fun getOnLoadingObservable(): Observable<Boolean> = onLoadingSubject.toSerialized()

    fun getOnApiErrorObservable(): Observable<ApiThrowable> = onApiErrorSubject.toSerialized()

    fun getSnackbarObservable(): Observable<String> = onSnackbarSubject.toSerialized()

    protected fun <T : Any> Observable<T>.subscribeAndDispose(
        action: (T) -> Unit,
    ) {
        subscribeAndDispose<T, Throwable>(action = action)
    }

    protected fun <T : Any, AT : Throwable> Observable<T>.subscribeAndDispose(
        clazz: Class<AT>? = null,
        action: (T) -> Unit,
    ) {
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                action.invoke(it)
            }, {
                handleSubscribeError(it, clazz)
            })
            .also(::addDisposable)
    }

    protected fun <T : Any> Single<T>.subscribeAndDispose(action: (T) -> Unit) {
        subscribeAndDispose<T, ApiThrowable>(action = action)
    }

    protected fun <T : Any> Single<T>.subscribeAndDispose(
        compositeDisposable: CompositeDisposable,
        action: (T) -> Unit,
    ) {
        subscribeAndDispose<T, ApiThrowable>(
            action = action,
            compositeDisposable = compositeDisposable
        )
    }

    protected fun <T : Any, AT : ApiThrowable> Single<T>.subscribeAndDispose(
        clazz: Class<AT>? = null,
        errorAction: (Throwable) -> Unit = {},
        compositeDisposable: CompositeDisposable? = null,
        action: (T) -> Unit,
    ) {
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                action.invoke(it)
            }, {
                errorAction.invoke(it)
                handleSubscribeError(it, clazz)
            })
            .also {
                if (compositeDisposable != null) {
                    compositeDisposable.add(it)
                } else {
                    addDisposable(it)
                }
            }
    }

    protected fun Completable.subscribeAndDispose(action: () -> Unit) {
        subscribeAndDispose<ApiThrowable>(action = action)
    }

    protected fun <AT : ApiThrowable> Completable.subscribeAndDispose(
        clazz: Class<AT>? = null,
        errorAction: (Throwable) -> Unit = {},
        action: () -> Unit,
    ) {
        subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                action.invoke()
            }, {
                errorAction.invoke(it)
                handleSubscribeError(it, clazz)
            })
            .also(::addDisposable)
    }

    private fun <AT : Throwable> handleSubscribeError(
        throwable: Throwable,
        clazz: Class<AT>? = null,
    ) {
        if (throwable is ApiThrowable) {
            if (throwable::class.java == clazz) {
                handleError(throwable)
            } else {
                handleError(throwable)
            }
        } else {
            handleError(throwable)
        }

        printError(throwable)
        setLoading(false)
    }
}