package com.ligo.feature.senderparcel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ligo.common.BaseFragment
import com.ligo.common.ligo.FragmentHelper
import com.ligo.common.map.MapContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.Location
import com.ligo.data.model.LocationUpdate
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelStatus
import com.ligo.feature.sendertrip.databinding.FragmentSenderParcelBinding
import com.ligo.navigator.api.Target
import com.ligo.subfeature.parceltype.ParcelTypeInfoBottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class SenderParcelFragment : BaseFragment<SenderParcelFragmentViewModel>(), MapContainer {

    companion object {
        const val TAG_BACKSTACK = "SenderTripFragment"

        private const val ARGS_PARCEL_ID = "args_parcel"

        fun newInstance(parcelId: String): Fragment {
            return SenderParcelFragment().apply {
                arguments = bundleOf(
                    ARGS_PARCEL_ID to parcelId
                )
            }
        }
    }

    override val koinModule: Module = SenderParcelModule
    override val viewModel by inject<SenderParcelFragmentViewModel>()

    override var mapView: MapView? = null
    override var googleMap: GoogleMap? = null
    override val isMyLocationEnabled: Boolean = false

    private var binding: FragmentSenderParcelBinding? = null

    private var driverMarker: Marker? = null
    private var parcel: Parcel? = null

    private val fragmentHelper by lazy {
        FragmentHelper(navigator, localizationManager, startedCompositeDisposable)
    }

    private val parcelId by lazy { arguments?.getString(ARGS_PARCEL_ID).orEmpty() }

    override fun onChildFragmentStarted(f: Fragment) {
        when (f) {
            is ParcelDeliveredBottomSheetDialogFragment -> {
                f.getRatingObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, ::handleRating)
            }
        }
    }

    private fun handleRating(rating: Int) {
        parcel?.driver?.let { viewModel.updateUserRating(it._id, rating) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoading)

        viewModel.getOnDeliveryLinkObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleDeliveryLink)

        viewModel.getOnLocationUpdateObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLocationUpdate)

        viewModel.getOnParcelDeliveredSubject()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::showPackageDeliveredBottomSheet)

        viewModel.getOnRouteObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleRoute)
    }

    private fun handleLoading(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun handleDeliveryLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"

        intent.putExtra(Intent.EXTRA_TEXT, link)
        startActivity(
            Intent.createChooser(
                intent,
                localizationManager.getLocalized(ConfigStringKey.SHARE_USING)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSenderParcelBinding.inflate(inflater, container, false)
        binding?.apply {
            tvContactInfoLabel.setLocalizedTextByKey(ConfigStringKey.CONTACT_INFO)
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
                parcel?.driver?.location?.apply(::showDriverMarker)
                parcel?.apply { googleMap?.animateCamera(getCameraUpdateFactory(this)) }
            }

            tvHeader.setLocalizedTextByKey(ConfigStringKey.PARCEL_INFO)
            tvTripInfo.setLocalizedTextByKey(ConfigStringKey.TRIP_ON_MAP)
            tvShowQr.setLocalizedTextByKey(ConfigStringKey.SHOW_QR)
            tvShareDeliveryLink.setLocalizedTextByKey(ConfigStringKey.SHARE_DELIVERY_LINK)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            tvChatWithDriver.setLocalizedTextByKey("Chat with Driver")
        }
    }

    override fun onStart() {
        super.onStart()

        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }

            tvPhone.setOnThrottleClickListener(startedCompositeDisposable) {
                val phone = tvPhone.text
                if (phone.isNotEmpty()) navigator.open(Target.PhoneCallApp(phone))
            }

            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(SenderParcelFragment::class.java)
            }

            clChatWithDriver.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.openChatForParcel(parcelId)
            }
        }

        viewModel.getChatAvailableObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable) { isAvailable ->
                binding?.clChatWithDriver?.isVisible = isAvailable
            }

        viewModel.getUnreadMessagesCountObservable(parcelId)
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleUnreadMessageCount)

        viewModel.getParcelObservable(parcelId)
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleParcel)
    }

    private fun handleUnreadMessageCount(count: Int) {
        binding?.tvUnreadMessagesCount?.isVisible = count != 0
        binding?.tvUnreadMessagesCount?.text = count.toString()
    }

    private fun handleParcel(parcelOptional: Optional<Parcel>) {
        val parcel = parcelOptional.getOrNull() ?: return
        this.parcel = parcel

        withBindingSafety(binding) {
            when (parcel.status) {
                ParcelStatus.ACCEPTED -> {
                    clShowQr.isVisible = true
                    clShareDeliveryLink.isVisible = false
                }

                ParcelStatus.PICKED -> {
                    clShowQr.isVisible = false
                    clShareDeliveryLink.isVisible = true
                }

                ParcelStatus.DELIVERED -> {
                    binding?.clShowQr?.isVisible = false
                    binding?.clShareDeliveryLink?.isVisible = false
                }

                else -> {
                    clShowQr.isVisible = false
                    clShareDeliveryLink.isVisible = false
                }
            }

            clShowQr.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.showQr(parcelId)
            }

            clShareDeliveryLink.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.generateDeliveryLink(parcel._id)
            }

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

            viewModel.fetchDirection(parcel.startPoint, parcel.endPoint)
            googleMap?.animateCamera(getCameraUpdateFactory(parcel))

            val driver = parcel.driver ?: return@withBindingSafety
            tvPhone.text = driver.phone
            fragmentHelper.setAvatarAndRating(clAvatarAndRating, driver)
        }
    }

    private fun showPackageDeliveredBottomSheet(parcel: Parcel) {
        if (parcelId != parcel._id) return
        ParcelDeliveredBottomSheetDialogFragment.newInstance()
            .show(childFragmentManager, ParcelDeliveredBottomSheetDialogFragment.TAG)
    }

    private fun handleLocationUpdate(locationUpdate: LocationUpdate) {
        if (parcel?.driver?._id == locationUpdate.userId) {
            showDriverMarker(locationUpdate.location)
        }
    }

    private fun showDriverMarker(location: Location) {
        val marker = MarkerOptions().position(LatLng(location.latitude, location.longitude))
            .icon(BitmapDescriptorFactory.fromResource(com.ligo.common.R.drawable.driver_location))
        driverMarker?.remove()
        driverMarker = googleMap?.addMarker(marker)
    }
}