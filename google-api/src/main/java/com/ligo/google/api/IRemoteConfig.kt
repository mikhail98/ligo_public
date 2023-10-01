package com.ligo.google.api

import com.ligo.core.Initializable
import io.reactivex.rxjava3.core.Observable

interface IRemoteConfig : Initializable {

    companion object {
        const val UPDATE_APP_RECOMMENDATION_VERSION = "update_app_recommendation_version"
        const val UPDATE_APP_MANDATORY_VERSION = "update_app_mandatory_version"
        const val SHOW_NOT_ENOUGH_DRIVERS = "show_not_enough_drivers_beta"
        const val DEFAULT_CURRENCY = "default_currency"
        const val CURRENCIES = "currencies"
        const val PARCEL_TYPES = "parcel_types"
        const val CHAT_ENABLED = "chat_enabled"
    }

    fun getFetchAndActivateCompleteObservable(): Observable<Boolean>

    fun getBoolean(key: String): Boolean

    fun getString(key: String): String

    fun getLong(key: String): Long

    fun getCurrencyList(): List<RemoteConfigCurrency>

    fun getParcelTypeList(): List<RemoteConfigParcelType>
}