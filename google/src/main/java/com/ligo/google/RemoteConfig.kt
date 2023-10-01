package com.ligo.google

import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.ligo.core.BuildConfig
import com.ligo.core.Initializable
import com.ligo.google.api.IRemoteConfig
import com.ligo.google.api.RemoteConfigCurrencies
import com.ligo.google.api.RemoteConfigCurrency
import com.ligo.google.api.RemoteConfigParcelType
import com.ligo.google.api.RemoteConfigParcelTypes
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

internal class RemoteConfig : IRemoteConfig {

    override val initOnList: List<Initializable.On> = listOf(Initializable.On.CREATE)
    override val connectOnList: List<Initializable.On> = listOf(Initializable.On.NEVER)
    override val clearOnList: List<Initializable.On> = listOf(Initializable.On.NEVER)

    private val remoteConfig = Firebase.remoteConfig

    private var initCalled = false

    private var fetchAndActivateTask: Task<Boolean>? = null
    private val fetchAndActivateCompleteSubject: Subject<Boolean> =
        BehaviorSubject.createDefault(false).toSerialized()

    @Synchronized
    override fun init() {
        if (initCalled) return
        initCalled = true

        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = 3600L }
        )
        remoteConfig.setDefaultsAsync(
            mapOf(
                IRemoteConfig.UPDATE_APP_RECOMMENDATION_VERSION to BuildConfig.VERSION_CODE,
                IRemoteConfig.UPDATE_APP_MANDATORY_VERSION to BuildConfig.VERSION_CODE,
                IRemoteConfig.SHOW_NOT_ENOUGH_DRIVERS to true,
                IRemoteConfig.DEFAULT_CURRENCY to "PLN",
                IRemoteConfig.CURRENCIES to RemoteConfigDefaults.DEFAULT_CURRENCIES,
                IRemoteConfig.PARCEL_TYPES to RemoteConfigDefaults.DEFAULT_PARCEL_TYPES,
                IRemoteConfig.CHAT_ENABLED to false
            )
        )

        fetchAndActivateTask = Firebase.remoteConfig.fetchAndActivate().also {
            it.addOnCompleteListener {
                fetchAndActivateCompleteSubject.onNext(true)
            }
        }
    }

    override fun getFetchAndActivateCompleteObservable(): Observable<Boolean> =
        fetchAndActivateCompleteSubject

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }

    override fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    override fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }

    override fun getCurrencyList(): List<RemoteConfigCurrency> {
        return get(
            IRemoteConfig.CURRENCIES,
            RemoteConfigDefaults.DEFAULT_CURRENCIES,
            RemoteConfigCurrencies::class.java
        ).currencies.map {
            RemoteConfigCurrency.fromCode(it.code, it.fullNameKey)
        }
    }

    override fun getParcelTypeList(): List<RemoteConfigParcelType> {
        return get(
            IRemoteConfig.PARCEL_TYPES,
            RemoteConfigDefaults.DEFAULT_PARCEL_TYPES,
            RemoteConfigParcelTypes::class.java
        ).typeList
    }

    private fun <T> get(key: String, defaultValue: String, clazz: Class<T>): T {
        val string = getString(key)
        return if (string.isEmpty()) {
            Gson().fromJson(defaultValue, clazz)
        } else {
            try {
                Gson().fromJson(string, clazz)
            } catch (e: Exception) {
                Gson().fromJson(defaultValue, clazz)
            }
        }
    }
}