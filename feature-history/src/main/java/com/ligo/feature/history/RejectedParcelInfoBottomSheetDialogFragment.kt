package com.ligo.feature.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.core.loadImageWithGlide
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.REJECTION_INFO_DRIVER_MESSAGE_TITLE
import com.ligo.data.model.ConfigStringKey.REJECTION_INFO_POPUP_DESCRIPTION
import com.ligo.data.model.ConfigStringKey.REJECTION_INFO_POPUP_TITLE
import com.ligo.data.model.Parcel
import com.ligo.feature.history.databinding.FragmentBottomSheetRejectedParcelInfoBinding
import org.koin.android.ext.android.inject
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

internal class RejectedParcelInfoBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<RejectedParcelInfoBottomSheetViewModel>() {

    companion object {

        const val TAG = "RejectParcelInfoBottomSheetDialogFragment"

        private const val REJECTED_PARCEL_ID_ARGS = "REJECTED_PARCEL_ID_ARGS"

        fun newInstance(parcelId: String): RejectedParcelInfoBottomSheetDialogFragment {
            return RejectedParcelInfoBottomSheetDialogFragment().apply {
                arguments = bundleOf(REJECTED_PARCEL_ID_ARGS to parcelId)
            }
        }
    }

    override val koinModule = HistoryModule
    override val viewModel by inject<RejectedParcelInfoBottomSheetViewModel>()
    private var binding: FragmentBottomSheetRejectedParcelInfoBinding? = null
    private val parcelId by lazy { arguments?.getString(REJECTED_PARCEL_ID_ARGS) ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetRejectedParcelInfoBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.getOnParcelObservable(parcelId)
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleParcel)
    }

    private fun handleParcel(parcel: Optional<Parcel>) {
        val rejectedParcel = parcel.getOrNull() ?: return
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(REJECTION_INFO_POPUP_TITLE)
            tvDescription.setLocalizedTextByKey(REJECTION_INFO_POPUP_DESCRIPTION)

            val rejectReasonKey = rejectedParcel.rejectReason?.getConfigStringKey()
            tvDescription.isVisible = rejectReasonKey != null

            tvDriverMessage.isVisible = rejectedParcel.rejectComment != null
            tvDriverMessageTitle.setLocalizedTextByKey(REJECTION_INFO_DRIVER_MESSAGE_TITLE)

            btnGotIt.clipToOutline = true
            btnGotIt.setLocalizedTextByKey(ConfigStringKey.GOT_IT)

            tvRejectionReason.displayLocalizedText(rejectReasonKey)

            val isDriverMessageNotEmpty =
                rejectedParcel.rejectComment != null || rejectedParcel.rejectPickupPhotoUrl != null

            clRejectionInfo.isVisible = isDriverMessageNotEmpty

            tvDriverMessage.text = rejectedParcel.rejectComment

            rejectedParcel.rejectPickupPhotoUrl?.let(ivRejectPhoto::loadImageWithGlide)

            btnGotIt.setOnThrottleClickListener(startedCompositeDisposable) {
                dismiss()
            }
        }
    }
}
