package com.ligo.feature.searchfordriver

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.ligo.common.BaseFragment
import com.ligo.common.ligo.FragmentHelper
import com.ligo.common.map.MapContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.alert.showDialog
import com.ligo.common.ui.alert.showNotEnoughDriversDialog
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.CANCEL_PARCEL_REQUEST
import com.ligo.data.model.Parcel
import com.ligo.feature.searchfordriver.databinding.FragmentSearchForDriverBinding
import com.ligo.subfeature.parceltype.ParcelTypeInfoBottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class SearchForDriverFragment : BaseFragment<SearchForDriverFragmentViewModel>(), MapContainer {

    companion object {

        const val PULSE_FADE_ANIM_DURATION = 2000L

        const val TAG_BACKSTACK = "SearchForDriverFragment"
        const val ARGS_PARCEL_ID = "args_parcel_id"

        fun newInstance(parcelId: String): Fragment {
            return SearchForDriverFragment().apply {
                arguments = bundleOf(ARGS_PARCEL_ID to parcelId)
            }
        }
    }

    override val koinModule: Module = SearchForDriverModule
    override val viewModel by inject<SearchForDriverFragmentViewModel>()

    override var googleMap: GoogleMap? = null
    override var mapView: MapView? = null
    override val isMyLocationEnabled: Boolean = false

    private var binding: FragmentSearchForDriverBinding? = null

    private val fragmentHelper by lazy {
        FragmentHelper(navigator, localizationManager, startedCompositeDisposable)
    }

    private var parcel: Parcel? = null

    private val parcelId: String by lazy { arguments?.getString(ARGS_PARCEL_ID).orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoading)

        viewModel.getOnRouteObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleParcelRoute)

        viewModel.getOnShowEnoughDriverDialogObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) {
                context?.showNotEnoughDriversDialog(localizationManager)
            }
    }

    private fun handleLoading(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun handleParcelRoute(route: List<LatLng>) {
        handleRoute(route)
        binding?.animationView?.setVisibilityWithAlpha(true, PULSE_FADE_ANIM_DURATION)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchForDriverBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            setupMap(savedInstanceState, mapView)
            tvTitle.setLocalizedTextByKey(ConfigStringKey.SEARCH_FOR_DRIVER_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.SEARCH_FOR_DRIVER_DESCRIPTION)
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true

            btnCancelParcel.clipToOutline = true
            btnCancelParcel.setLocalizedTextByKey(CANCEL_PARCEL_REQUEST)
        }
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            btnCancelParcel.setOnThrottleClickListener(startedCompositeDisposable) {
                val title =
                    localizationManager.getLocalized(ConfigStringKey.PARCEL_CANCELLATION_DIALOG_TITLE)
                val message =
                    localizationManager.getLocalized(ConfigStringKey.PARCEL_CANCELLATION_DIALOG_MESSAGE)
                val okBtnText =
                    localizationManager.getLocalized(ConfigStringKey.CONFIRM)
                val cancelBtnText =
                    localizationManager.getLocalized(ConfigStringKey.CANCEL)
                context?.showDialog(title, message, okBtnText, cancelBtnText) {
                    viewModel.cancelParcel()
                }
            }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(SearchForDriverFragment::class.java)
            }
        }

        viewModel.getParcelObservable(parcelId)
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleParcel)
    }

    private fun handleParcel(parcelOptional: Optional<Parcel>) {
        val parcel = parcelOptional.getOrNull() ?: return
        this.parcel = parcel

        withBindingSafety(binding) {
            fragmentHelper.setRouteInfo(
                clOrderInfo,
                parcel.startPoint,
                parcel.endPoint,
                parcel.parcelPhoto
            )
            fragmentHelper.setParcelParams(clParcelParams, parcel) {
                ParcelTypeInfoBottomSheetDialogFragment.newInstance()
                    .show(childFragmentManager, ParcelTypeInfoBottomSheetDialogFragment.TAG)
            }
        }

        viewModel.fetchDirection(parcel.startPoint, parcel.endPoint)
        googleMap?.animateCamera(getCameraUpdateFactory(parcel))
    }
}
