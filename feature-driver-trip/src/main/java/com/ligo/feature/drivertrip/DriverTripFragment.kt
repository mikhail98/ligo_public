package com.ligo.feature.drivertrip

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.ligo.common.BaseFragment
import com.ligo.common.ligo.FragmentHelper
import com.ligo.common.map.MapContainer
import com.ligo.common.model.ParcelInTripUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.alert.showDialog
import com.ligo.common.withBindingSafety
import com.ligo.core.getTimeInMillis
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelStatus
import com.ligo.data.model.Trip
import com.ligo.data.model.TripStatus
import com.ligo.feature.drivertrip.databinding.FragmentDriverTripBinding
import com.ligo.feature.drivertrip.recycler.ParcelInTripAdapter
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.subfeature.parcelavailable.ParcelAvailableBottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class DriverTripFragment : BaseFragment<DriverTripFragmentViewModel>(), MapContainer {

    companion object {

        const val TAG_BACKSTACK = "driver_trip"

        private const val ARGS_TRIP_ID = "args_trip_id"

        private const val DEFAULT_CHRONOMETER_FORMAT = "%s"

        fun newInstance(tripId: String): Fragment {
            return DriverTripFragment().apply {
                arguments = bundleOf(ARGS_TRIP_ID to tripId)
            }
        }
    }

    override val koinModule: Module = DriverTripModule
    override val viewModel by inject<DriverTripFragmentViewModel>()

    override var googleMap: GoogleMap? = null
    override var mapView: MapView? = null
    override val isMyLocationEnabled: Boolean = true

    private var binding: FragmentDriverTripBinding? = null

    private val adapter by lazy { ParcelInTripAdapter(localizationManager) }
    private val fragmentHelper by lazy {
        FragmentHelper(navigator, localizationManager, startedCompositeDisposable)
    }

    private var trip: Trip? = null
    private var parcels: List<ParcelInTripUiModel>? = null
    private val cancelableStatuses = listOf(ParcelStatus.DELIVERED, ParcelStatus.REJECTED)

    private val tripId by lazy { arguments?.getString(ARGS_TRIP_ID).orEmpty() }

    override fun onChildFragmentStarted(f: Fragment) {
        when (f) {
            is ParcelAvailableBottomSheetDialogFragment -> {
                f.getOnAcceptObservable().map { it._id }
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::acceptParcel)

                f.getOnDeclineObservable().map { it._id }
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::declineParcel)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoading)

        viewModel.getOnRouteObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleRoute)

        viewModel.getAvailableParcelObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleAvailableParcel)

        viewModel.getOnTripStartedObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) { updateStartTripBtn() }

        adapter.getOnItemClickedObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) {
                navigator.open(Target.ParcelInTrip(it.parcelId))
            }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getTripObservable(tripId)
            .subscribeAndDisposeAt(startedCompositeDisposable) { handleTrip(it.first, it.second) }

        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(DriverTripFragment::class.java)
            }
        }
    }

    private fun handleParcels(parcelList: List<ParcelInTripUiModel>) {
        val hasParcels = parcelList.isNotEmpty()
        binding?.tvTitle?.isVisible = !hasParcels
        binding?.tvDescription?.isVisible = !hasParcels
        binding?.tvParcelsLabel?.isVisible = hasParcels
        binding?.rvParcels?.isVisible = hasParcels

        adapter.setItems(parcelList)
        updateCancelButtonState()
    }

    private fun handleLoading(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun handleAvailableParcel(parcel: Parcel) {
        ParcelAvailableBottomSheetDialogFragment.newInstance(parcel)
            .show(childFragmentManager, ParcelAvailableBottomSheetDialogFragment.TAG)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDriverTripBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvParcelsLabel.setLocalizedTextByKey(ConfigStringKey.PARCELS)
            tvHeader.setLocalizedTextByKey(ConfigStringKey.CURRENT_TRIP)
            tvTitle.setLocalizedTextByKey(ConfigStringKey.WAITING_FOR_PARCEL_REQUEST_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.WAITING_FOR_PARCEL_REQUEST_DESCRIPTION)
            btnFinishTrip.setLocalizedTextByKey(ConfigStringKey.FINISH_TRIP)
            btnStartTrip.setLocalizedTextByKey(ConfigStringKey.START_TRIP)
            setupMap(savedInstanceState, mapView)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            btnStartTrip.clipToOutline = true
            btnFinishTrip.clipToOutline = true
            rvParcels.adapter = adapter
        }
    }

    private fun handleTrip(
        tripOptional: Optional<Trip>,
        parcelOptional: Optional<List<ParcelInTripUiModel>>
    ) {
        val trip = tripOptional.getOrNull() ?: return
        val parcels = parcelOptional.getOrNull() ?: emptyList<ParcelInTripUiModel>()
        this.trip = trip
        this.parcels = parcels

        if (trip.status == TripStatus.ACTIVE) {
            viewModel.checkLocationService()
        } else {
            viewModel.stopLocationService()
        }

        handleParcels(parcels)

        viewModel.fetchDirection(trip.startPoint, trip.endPoint)
        googleMap?.animateCamera(getCameraUpdateFactory(trip))

        withBindingSafety(binding) {
            btnStartTrip.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.startTrip(trip._id)
            }
            btnFinishTrip.setOnThrottleClickListener(startedCompositeDisposable) {
                onFinishTripClick(trip)
            }
            fragmentHelper.setRouteInfo(clOrderInfo, trip.startPoint, trip.endPoint, null)
        }

        setUpChronometer(trip)
        updateStartTripBtn()

        val pushArgs = getPushArgs()
        viewModel.fetchAvailableParcel(pushArgs.availableParcelId)

        val newPushArgs = pushArgs.copy(availableParcelId = null)
        activity?.intent?.putExtra(INavigator.EXTRA_PUSH_ARGS, newPushArgs)
    }

    private fun onFinishTripClick(trip: Trip) {
        val title =
            localizationManager.getLocalized(ConfigStringKey.TRIP_FINISHING_DIALOG_TITLE)
        val message =
            localizationManager.getLocalized(ConfigStringKey.TRIP_FINISHING_DIALOG_MESSAGE)
        val okBtnText = localizationManager.getLocalized(ConfigStringKey.FINISH)
        val cancelBtnText = localizationManager.getLocalized(ConfigStringKey.CANCEL)

        context?.showDialog(title, message, okBtnText, cancelBtnText) {
            viewModel.finishTrip(trip._id)
        }
    }

    private fun updateCancelButtonState() {
        val trip = trip ?: return
        val separatorLine = binding?.vLine ?: return

        val canCancel = trip.parcelList.all { cancelableStatuses.contains(it.status) }
        binding?.btnFinishTrip?.isVisible = canCancel
        separatorLine.isVisible = canCancel

        updateButtonsCl()
    }

    private fun setUpChronometer(trip: Trip) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = SystemClock.elapsedRealtime()
        val createdTime = getTimeInMillis(trip.createdAt)
        val tripDuration = currentTime - createdTime
        val baseTime = elapsedTime - tripDuration

        withBindingSafety(binding) {
            chronometer.base = baseTime
            chronometer.format = DEFAULT_CHRONOMETER_FORMAT

            chronometer.start()
            when (trip.status) {
                TripStatus.FINISHED, TripStatus.UNKNOWN -> chronometer.stop()
                else -> Unit
            }
        }
    }

    private fun updateStartTripBtn() {
        binding?.btnStartTrip?.isVisible = trip?.status == TripStatus.SCHEDULED
        updateButtonsCl()
    }

    private fun updateButtonsCl() {
        withBindingSafety(binding) {
            clButtons.isVisible = btnStartTrip.isVisible || btnFinishTrip.isVisible
        }
    }
}