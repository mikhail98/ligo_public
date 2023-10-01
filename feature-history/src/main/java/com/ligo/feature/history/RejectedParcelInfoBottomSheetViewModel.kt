package com.ligo.feature.history

import com.ligo.common.BaseViewModel
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.model.Parcel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable
import java.util.Optional

internal class RejectedParcelInfoBottomSheetViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val parcelsCoordinator: ISenderParcelsCoordinator
) : BaseViewModel(navigator, analytics) {

    fun getOnParcelObservable(parcelId: String): Observable<Optional<Parcel>> {
        return parcelsCoordinator.getParcelListObservable()
            .map { Optional.ofNullable(it.firstOrNull { parcel -> parcel._id == parcelId }) }
    }
}
