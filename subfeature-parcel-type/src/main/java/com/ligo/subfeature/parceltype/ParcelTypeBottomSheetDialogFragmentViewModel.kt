package com.ligo.subfeature.parceltype

import com.ligo.common.BaseViewModel
import com.ligo.common.model.ParcelTypeUiModel
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IRemoteConfig
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class ParcelTypeBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val remoteConfig: IRemoteConfig,
) : BaseViewModel(navigator, analytics) {

    private val parcelTypeUiListSubject: Subject<List<ParcelTypeUiModel>> =
        PublishSubject.create()

    fun fetchParcelTypeUiList(selectedParcelTypeList: List<String>) {
        val parcelTypeUiList = remoteConfig.getParcelTypeList()
            .map { ParcelTypeUiModel.fromRemoteConfigParcelType(it, selectedParcelTypeList) }
        parcelTypeUiListSubject.onNext(parcelTypeUiList)
    }

    fun getParcelTypeUiListObservable(): Observable<List<ParcelTypeUiModel>> =
        parcelTypeUiListSubject
}