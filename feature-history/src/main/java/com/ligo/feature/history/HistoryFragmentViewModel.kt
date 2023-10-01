package com.ligo.feature.history

import com.ligo.common.BaseViewModel
import com.ligo.common.model.HistoryUiModel
import com.ligo.common.model.StatusUiModel
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

class HistoryFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val tripsCoordinator: IDriverTripCoordinator,
    private val parcelsCoordinator: ISenderParcelsCoordinator,
) : BaseViewModel(navigator, analytics) {

    private var historyUiModelMap: MutableMap<String, HistoryUiModel> = mutableMapOf()

    private val historyUiModelListSubject: Subject<List<HistoryUiModel>> =
        BehaviorSubject.create<List<HistoryUiModel>>().toSerialized()

    override fun onCreate() {
        tripsCoordinator.getTripListObservable()
            .map { tripList -> tripList.map { HistoryUiModel.fromTrip(it) } }
            .subscribeAndDispose { newUiModelList ->
                newUiModelList.forEach {
                    historyUiModelMap[it.id] = it
                }
                triggerUpdateHistoryUiModelList()
            }

        parcelsCoordinator.getParcelListObservable()
            .map { parcelList -> parcelList.map { HistoryUiModel.fromParcel(it) } }
            .subscribeAndDispose { newUiModelList ->
                newUiModelList.forEach {
                    historyUiModelMap[it.id] = it
                }
                triggerUpdateHistoryUiModelList()
            }
    }

    private fun triggerUpdateHistoryUiModelList() {
        val values = historyUiModelMap.values.toList()
        val activeTrip = values.firstOrNull { it.uiStatus is StatusUiModel.TripActive }
        val other = values.filter { it.uiStatus !is StatusUiModel.TripActive }
        val list = buildList {
            activeTrip?.let(::add)
            addAll(other.sortedBy { it.createdAt }.asReversed())
        }
        historyUiModelListSubject.onNext(list)
    }

    fun getHistoryUiModelListObservable(): Observable<List<HistoryUiModel>> =
        historyUiModelListSubject
}