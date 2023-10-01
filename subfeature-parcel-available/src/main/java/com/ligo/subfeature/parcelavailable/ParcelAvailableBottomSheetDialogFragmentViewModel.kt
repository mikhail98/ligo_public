package com.ligo.subfeature.parcelavailable

import com.ligo.common.BaseViewModel
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.model.Parcel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable

class ParcelAvailableBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val tripCoordinator: IDriverTripCoordinator,
) :
    BaseViewModel(navigator, analytics) {

    fun getAvailableParcelCancelledObservable(): Observable<Parcel> =
        tripCoordinator.getAvailableParcelCancelledObservable()
}