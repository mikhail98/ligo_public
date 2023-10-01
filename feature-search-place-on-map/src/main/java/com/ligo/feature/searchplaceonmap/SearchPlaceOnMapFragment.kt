package com.ligo.feature.searchplaceonmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.ligo.common.BaseFragment
import com.ligo.common.map.MapContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.button.ActionButton
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.Location
import com.ligo.feature.searchplaceonmap.databinding.FragmentSearchPlaceOnMapBinding
import com.ligo.google.api.ILocationManager
import com.ligo.tools.api.SearchPlaceRequest
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class SearchPlaceOnMapFragment : BaseFragment<SearchPlaceOnMapFragmentViewModel>(), MapContainer {

    companion object {
        const val TAG_BACKSTACK = "search_place_on_map"

        private const val ARGS_ORIGIN = "args_origin"

        fun newInstance(origin: SearchPlaceRequest.Origin): Fragment {
            return SearchPlaceOnMapFragment().apply {
                arguments = bundleOf(ARGS_ORIGIN to origin)
            }
        }
    }

    override val koinModule: Module = SearchPlaceOnMapModule
    override val viewModel by inject<SearchPlaceOnMapFragmentViewModel>()
    private val locationManager by inject<ILocationManager>()

    override var googleMap: GoogleMap? = null
    override var mapView: MapView? = null
    override val isMyLocationEnabled: Boolean = true

    private var binding: FragmentSearchPlaceOnMapBinding? = null

    private val origin by lazy { arguments?.getSerializable(ARGS_ORIGIN) as SearchPlaceRequest.Origin }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoading)

        viewModel.getOnPlaceInfoObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLocation)
    }

    private fun handleLoading(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun handleLocation(dataOptional: Optional<Location>) {
        val data = dataOptional.getOrNull()
        val (title, address) = if (data != null) {
            data.fullName to data.address
        } else {
            val noData = localizationManager.getLocalized("NO_DATA")
            noData to noData
        }
        withBindingSafety(binding) {
            tvTitle.text = title
            tvDescription.text = address
            setSubmitBtnEnabled(data != null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchPlaceOnMapBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            btnSubmit.clipToOutline = true
            setupMap(savedInstanceState, mapView) {
                it.uiSettings.isZoomGesturesEnabled = true
                it.uiSettings.isScrollGesturesEnabled = true
                it.uiSettings.isRotateGesturesEnabled = false
                it.setOnCameraIdleListener { onMapCameraIdle() }
                locationManager.fetchLastKnownLocation { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    it.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
            }
            btnSubmit.setLocalizedTextByKey(ConfigStringKey.SUBMIT)
        }
        setSubmitBtnEnabled(false)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(SearchPlaceOnMapFragment::class.java)
            }
            btnSubmit.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.onPlaceSelected(origin)
            }
        }
    }

    private fun onMapCameraIdle() {
        val location = googleMap?.cameraPosition?.target ?: return
        viewModel.fetchPlaceInfo(location.latitude, location.longitude)
        setSubmitBtnEnabled(false)
    }

    private fun setSubmitBtnEnabled(isEnabled: Boolean) {
        withBindingSafety(binding) {
            btnSubmit.state = if (isEnabled) {
                ActionButton.State.PRIMARY
            } else {
                ActionButton.State.INACTIVE
            }
            progressSearch.isVisible = !isEnabled
        }
    }
}