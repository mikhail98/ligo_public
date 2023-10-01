package com.ligo.subfeature.parcelavailable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.ligo.FragmentHelper
import com.ligo.common.ligo.setAvatar
import com.ligo.common.ligo.setRating
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.core.loadImageWithGlide
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.Parcel
import com.ligo.subfeature.parcelavailable.databinding.FragmentBottomSheetAvailableParcelBinding
import com.ligo.subfeature.parceltype.ParcelTypeInfoBottomSheetDialogFragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ParcelAvailableBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<ParcelAvailableBottomSheetDialogFragmentViewModel>() {

    companion object {
        const val TAG = "parcel_available"

        private const val ARGS_PARCEL = "args_parcel"

        fun newInstance(parcel: Parcel): ParcelAvailableBottomSheetDialogFragment {
            return ParcelAvailableBottomSheetDialogFragment().apply {
                arguments = bundleOf(ARGS_PARCEL to parcel)
                isCancelable = false
            }
        }
    }

    private val declineSubject: Subject<Parcel> = PublishSubject.create<Parcel>().toSerialized()

    private val acceptSubject: Subject<Parcel> = PublishSubject.create<Parcel>().toSerialized()

    override val koinModule: Module = ParcelAvailableModule
    override val viewModel: ParcelAvailableBottomSheetDialogFragmentViewModel by inject()
    override val cancellable: Boolean = false

    private var binding: FragmentBottomSheetAvailableParcelBinding? = null

    private val fragmentHelper by lazy {
        FragmentHelper(navigator, localizationManager, startedCompositeDisposable)
    }

    private val parcel by lazy { arguments?.getParcelable<Parcel>(ARGS_PARCEL) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetAvailableParcelBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(ConfigStringKey.AVAILABLE_PARCEL)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.AVAILABLE_PARCEL_DESCRIPTION)
            btnAccept.setLocalizedTextByKey(ConfigStringKey.ACCEPT)
            btnDecline.setLocalizedTextByKey(ConfigStringKey.DECLINE)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        withBindingSafety(binding) {
            btnDecline.clipToOutline = true
            btnAccept.clipToOutline = true
        }
    }

    override fun onStart() {
        super.onStart()
        handleParcel(parcel ?: return)
        viewModel.getAvailableParcelCancelledObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable) { if (it._id == parcel?._id) dismiss() }
    }

    private fun handleParcel(parcel: Parcel) {
        withBindingSafety(binding) {
            tvSenderName.text = parcel.sender.name
            setAvatar(ivSenderAvatar, parcel.sender.avatarPhoto)
            ivImage.loadImageWithGlide(parcel.parcelPhoto.orEmpty())
            setRating(parcel.sender, rbRating, tvRating, localizationManager)
            fragmentHelper.setRouteInfo(clOrderInfo, parcel.startPoint, parcel.endPoint, null)
            fragmentHelper.setParcelParams(clParcelParams, parcel) {
                ParcelTypeInfoBottomSheetDialogFragment.newInstance()
                    .show(childFragmentManager, ParcelTypeInfoBottomSheetDialogFragment.TAG)
            }

            btnDecline.setOnThrottleClickListener(startedCompositeDisposable) {
                declineSubject.onNext(parcel)
                dismiss()
            }
            btnAccept.setOnThrottleClickListener(startedCompositeDisposable) {
                acceptSubject.onNext(parcel)
                dismiss()
            }
        }
    }

    fun getOnDeclineObservable(): Observable<Parcel> = declineSubject

    fun getOnAcceptObservable(): Observable<Parcel> = acceptSubject
}