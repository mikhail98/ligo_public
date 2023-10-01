package com.ligo.tools

import com.ligo.tools.api.IPlaceSearchManager
import com.ligo.tools.api.SearchPlaceResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class PlaceSearchManager : IPlaceSearchManager {

    private val pickPlaceSubject: Subject<SearchPlaceResult> =
        PublishSubject.create<SearchPlaceResult>().toSerialized()

    override fun pickPlace(result: SearchPlaceResult) {
        pickPlaceSubject.onNext(result)
    }

    override fun getOnPlacePickedObservable(): Observable<SearchPlaceResult> =
        pickPlaceSubject
}