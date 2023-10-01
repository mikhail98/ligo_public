package com.ligo.toggler.api

import io.reactivex.rxjava3.core.Observable

interface IToggler {

    fun getAvailableFeatureListObservable(): Observable<List<Feature>>
}