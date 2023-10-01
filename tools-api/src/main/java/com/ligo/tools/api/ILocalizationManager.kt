package com.ligo.tools.api

import io.reactivex.rxjava3.core.Completable

interface ILocalizationManager {

    fun updateLocalization(): Completable

    fun getLocalized(key: String): String
}