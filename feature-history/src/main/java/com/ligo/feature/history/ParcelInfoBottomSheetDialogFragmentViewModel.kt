package com.ligo.feature.history

import com.google.android.gms.maps.model.LatLng
import com.ligo.common.BaseViewModel
import com.ligo.common.map.MapUtil
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.model.Location
import com.ligo.data.model.Parcel
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Optional

class ParcelInfoBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val googleApisRepo: IGoogleApisRepo,
    private val parcelCoordinator: ISenderParcelsCoordinator,
) : BaseViewModel(navigator, analytics) {

    private val routeSubject: Subject<List<LatLng>> =
        BehaviorSubject.create<List<LatLng>>().toSerialized()

    private var parcelId: String? = null

    fun fetchDirection(startPoint: Location, endPoint: Location) {
        googleApisRepo.searchForDirection(startPoint, endPoint)
            .map { MapUtil.decodeRoutePath(it.points) }
            .subscribeAndDispose(routeSubject::onNext)
    }

    fun getOnRouteObservable(): Observable<List<LatLng>> = routeSubject

    fun getParcelObservable(parcelId: String): Observable<Optional<Parcel>> {
        this.parcelId = parcelId
        return parcelCoordinator.getParcelListObservable()
            .map { parcelList ->
                val parcel = parcelList.find { it._id == parcelId }
                Optional.ofNullable(parcel)
            }
    }
}