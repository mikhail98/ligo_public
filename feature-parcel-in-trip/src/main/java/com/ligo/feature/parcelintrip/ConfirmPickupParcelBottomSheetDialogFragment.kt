package com.ligo.feature.parcelintrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.ViewContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.parcelintrip.databinding.FragmentBottomSheetConfirmPickupParcelBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ConfirmPickupParcelBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<ConfirmPickupParcelBottomSheetDialogFragmentViewModel>(),
    ViewContainer {

    companion object {
        const val TAG = "confirm_pickup_parcel"
        private const val ARGS_PARCEL_ID = "parcel_id"

        fun getBundle(parcelId: String): Bundle {
            return bundleOf(
                ARGS_PARCEL_ID to parcelId
            )
        }

        fun newInstance(bundle: Bundle): ConfirmPickupParcelBottomSheetDialogFragment {
            return ConfirmPickupParcelBottomSheetDialogFragment().apply { arguments = bundle }
        }
    }

    private val onPickupParcelSubject: Subject<String> =
        PublishSubject.create<String>().toSerialized()

    private val onRejectParcelSubject: Subject<String> =
        PublishSubject.create<String>().toSerialized()

    override val koinModule: Module = ConfirmPickupParcelModule
    override val viewModel: ConfirmPickupParcelBottomSheetDialogFragmentViewModel by inject()

    private var binding: FragmentBottomSheetConfirmPickupParcelBinding? = null

    private val parcelId by lazy { arguments?.getString(ARGS_PARCEL_ID).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetConfirmPickupParcelBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnReject.clipToOutline = true
            btnPickup.clipToOutline = true

            btnReject.setLocalizedTextByKey(ConfigStringKey.REJECT)
            btnPickup.setLocalizedTextByKey(ConfigStringKey.PICKUP)
            tvTitle.setLocalizedTextByKey(ConfigStringKey.PICKUP_DIALOG_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.PICKUP_DIALOG_DESCRIPTION)
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            btnPickup.setOnThrottleClickListener(startedCompositeDisposable) {
                onPickupParcelSubject.onNext(parcelId)
                dismiss()
            }
            btnReject.setOnThrottleClickListener(startedCompositeDisposable) {
                onRejectParcelSubject.onNext(parcelId)
                dismiss()
            }
        }
    }

    fun getOnPickupParcelObservable(): Observable<String> = onPickupParcelSubject

    fun getOnRejectParcelObservable(): Observable<String> = onRejectParcelSubject
}