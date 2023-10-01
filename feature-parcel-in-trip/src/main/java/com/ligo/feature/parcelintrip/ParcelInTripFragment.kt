package com.ligo.feature.parcelintrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.ligo.FragmentHelper
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.CAN_T_DELIVER_THIS_PARCEL
import com.ligo.data.model.ConfigStringKey.CAN_T_PICK_UP_THIS_PARCEL
import com.ligo.data.model.ConfigStringKey.PARCEL_PICKED
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelStatus
import com.ligo.feature.parcelintrip.databinding.FragmentParcelInTripBinding
import com.ligo.navigator.api.Target
import com.ligo.subfeature.parceltype.ParcelTypeInfoBottomSheetDialogFragment
import com.ligo.subfeature.ratesender.RateSenderBottomSheetDialogFragment
import com.ligo.tools.api.ScanQR
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class ParcelInTripFragment : BaseFragment<ParcelInTripFragmentViewModel>() {

    companion object {
        const val TAG_BACKSTACK = "parcel_info"

        private const val ARGS_PARCEL_ID = "args_parcel_id"

        fun newInstance(parcelId: String): Fragment {
            return ParcelInTripFragment().apply {
                arguments = bundleOf(ARGS_PARCEL_ID to parcelId)
            }
        }
    }

    override val koinModule: Module = ParcelInTripModule
    override val viewModel by inject<ParcelInTripFragmentViewModel>()

    private var binding: FragmentParcelInTripBinding? = null

    private val fragmentHelper by lazy {
        FragmentHelper(navigator, localizationManager, startedCompositeDisposable)
    }

    private var parcel: Parcel? = null

    private val parcelId by lazy { arguments?.getString(ARGS_PARCEL_ID).orEmpty() }

    override fun onChildFragmentStarted(f: Fragment) {
        when (f) {
            is RateSenderBottomSheetDialogFragment -> {
                f.getRatingObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable) { rating ->
                        parcel?.sender?._id?.let { viewModel.updateSenderRating(it, rating) }
                    }
            }

            is ConfirmPickupParcelBottomSheetDialogFragment -> {
                f.getOnPickupParcelObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::pickupParcel)

                f.getOnRejectParcelObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable) {
                        navigator.open(Target.RejectParcel(it))
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getParcelQRScannedObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) { origin ->
                when (origin) {
                    ScanQR.Origin.DELIVERY -> handleParcelDelivered()
                    ScanQR.Origin.PICK -> showSnackByStringKey(PARCEL_PICKED)
                    else -> Unit
                }
            }

        viewModel.getOnCantPickParcelObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) {
                showSnackByStringKey(CAN_T_PICK_UP_THIS_PARCEL)
            }
        viewModel.getOnCantDeliverParcelObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) {
                showSnackByStringKey(CAN_T_DELIVER_THIS_PARCEL)
            }
        viewModel.getOnShowConfirmPickupDialogObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::showConfirmPickupDialog)
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(ParcelInTripFragment::class.java)
            }
            tvPhone.setOnThrottleClickListener(startedCompositeDisposable) {
                val phone = tvPhone.text
                if (phone.isNotEmpty()) navigator.open(Target.PhoneCallApp(phone))
            }
            clChatWithSender.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.openChatForParcel(parcelId)
            }
        }

        viewModel.getChatAvailableObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable) {
                binding?.clChatWithSender?.isVisible = it
            }

        viewModel.getUnreadMessagesCountObservable(parcelId)
            .subscribeAndDisposeAt(startedCompositeDisposable) {
                binding?.tvUnreadChatCount?.isVisible = it != 0
                binding?.tvUnreadChatCount?.text = it.toString()
            }

        viewModel.getParcelObservable(parcelId)
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleParcel)
    }

    private fun handleParcel(parcelOptional: Optional<Parcel>) {
        val parcel = parcelOptional.getOrNull() ?: return
        this.parcel = parcel

        binding?.apply {
            tvPhone.text = parcel.sender.phone
            fragmentHelper.setAvatarAndRating(clAvatarAndRating, parcel.sender)
            clQRCode.isVisible = parcel.status != ParcelStatus.DELIVERED

            clQRCode.setOnThrottleClickListener(startedCompositeDisposable) {
                val origin = if (parcel.status == ParcelStatus.ACCEPTED) {
                    ScanQR.Origin.PICK
                } else {
                    ScanQR.Origin.DELIVERY
                }
                viewModel.scanQr(origin)
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentParcelInTripBinding.inflate(inflater, container, false)

        withBindingSafety(binding) {
            tvContactInfoLabel.setLocalizedTextByKey(ConfigStringKey.CONTACT_INFO)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            tvHeader.setLocalizedTextByKey(ConfigStringKey.PARCEL_INFO_TITLE)
            tvScanQr.setLocalizedTextByKey(ConfigStringKey.SCAN_QR)
            tvChatWithSender.setLocalizedTextByKey("Chat with sender")
        }
    }

    private fun handleParcelDelivered() {
        binding?.clQRCode?.isVisible = false
        RateSenderBottomSheetDialogFragment.newInstance()
            .show(childFragmentManager, RateSenderBottomSheetDialogFragment.TAG)
    }

    private fun showConfirmPickupDialog(parcelId: String) {
        val bundle = ConfirmPickupParcelBottomSheetDialogFragment.getBundle(parcelId)
        ConfirmPickupParcelBottomSheetDialogFragment.newInstance(bundle)
            .show(childFragmentManager, ConfirmPickupParcelBottomSheetDialogFragment.TAG)
    }
}