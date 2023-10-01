package com.ligo.subfeature.createparcel.currency

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IRemoteConfig
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class CurrencySelectionBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val remoteConfig: IRemoteConfig,
) : BaseViewModel(navigator, analytics) {

    private val currencyItemUiListSubject: Subject<List<CurrencyItemUi>> = PublishSubject.create()

    fun fetchCurrencyItemUiList(selectedCurrencyCode: String) {
        val currencyItemUiList = remoteConfig.getCurrencyList()
            .map { CurrencyItemUi(it, it.code == selectedCurrencyCode) }
        currencyItemUiListSubject.onNext(currencyItemUiList)
    }

    fun getCurrencyItemUiListObservable(): Observable<List<CurrencyItemUi>> = currencyItemUiListSubject
}