package com.ligo.feature.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.ligo.FragmentHelper
import com.ligo.common.map.MapContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.Parcel
import com.ligo.feature.history.databinding.FragmentBottomSheetParcelInfoBinding
import com.ligo.navigator.api.Target
import com.ligo.subfeature.parceltype.ParcelTypeInfoBottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class ParcelInfoBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<ParcelInfoBottomSheetDialogFragmentViewModel>(), MapContainer {

    companion object {

        const val TAG = "ParcelInfoBottomSheetDialog"

        private const val ARGS_PARCEL_ID = "parcel.id"

        fun newInstance(parcelId: String): ParcelInfoBottomSheetDialogFragment {
            return ParcelInfoBottomSheetDialogFragment().apply {
                arguments = bundleOf(ARGS_PARCEL_ID to parcelId)
            }
        }
    }

    override val koinModule: Module = ParcelInfoModule
    override val viewModel by inject<ParcelInfoBottomSheetDialogFragmentViewModel>()

    override val isMyLocationEnabled = false
    override var googleMap: GoogleMap? = null
    override var mapView: MapView? = null

    private var binding: FragmentBottomSheetParcelInfoBinding? = null

    private val fragmentHelper by lazy {
        FragmentHelper(navigator, localizationManager, startedCompositeDisposable)
    }

    private var parcel: Parcel? = null

    private val parcelId by lazy { arguments?.getString(ARGS_PARCEL_ID).orEmpty() }

    private fun handleParcel(parcelOptional: Optional<Parcel>) {
        val parcel = parcelOptional.getOrNull() ?: return
        this.parcel = parcel

        viewModel.fetchDirection(parcel.startPoint, parcel.endPoint)
        googleMap?.animateCamera(getCameraUpdateFactory(parcel))

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOnRouteObservable()
            .subscribeAndDisposeAt(resumedCompositeDisposable, ::handleRoute)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetParcelInfoBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            setupMap(savedInstanceState, mapView) {
                googleMap?.setOnMarkerClickListener { marker ->
                    val position = marker.position
                    navigator.open(
                        Target.MapApp(
                            position.latitude,
                            position.longitude,
                            "Driver location"
                        )
                    )
                    true
                }
            }
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        withBindingSafety(binding) {
            tvHeader.setLocalizedTextByKey(ConfigStringKey.PARCEL_INFO_TITLE)
            tvTripInfo.setLocalizedTextByKey(ConfigStringKey.TRIP_ON_MAP)
        }
    }

    override fun onStart() {
        super.onStart()

        withBindingSafety(binding) {
            btnSubmit.setLocalizedTextByKey(ConfigStringKey.GOT_IT)
            btnSubmit.setOnThrottleClickListener(startedCompositeDisposable) { dismiss() }
        }

        viewModel.getParcelObservable(parcelId)
            .subscribeAndDisposeAt(resumedCompositeDisposable, ::handleParcel)
    }
}