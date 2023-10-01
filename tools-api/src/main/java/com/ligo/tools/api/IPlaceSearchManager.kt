package com.ligo.tools.api

import io.reactivex.rxjava3.core.Observable

interface IPlaceSearchManager {

    fun pickPlace(result: SearchPlaceResult)

    fun getOnPlacePickedObservable(): Observable<SearchPlaceResult>
}