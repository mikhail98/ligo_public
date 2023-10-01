package com.ligo.feature.senderparcel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.sendertrip.databinding.FragmentBottomSheetParcelDeliveredBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ParcelDeliveredBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<ParcelDeliveredBottomSheetDialogFragmentViewModel>() {

    companion object {
        const val TAG = "parcel_delivered"

        fun newInstance(): BottomSheetDialogFragment {
            return ParcelDeliveredBottomSheetDialogFragment()
        }
    }

    private val ratingSubject: Subject<Int> = PublishSubject.create<Int>().toSerialized()

    override val koinModule: Module = ParcelDeliveredModule
    override val viewModel: ParcelDeliveredBottomSheetDialogFragmentViewModel by inject()

    private var binding: FragmentBottomSheetParcelDeliveredBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetParcelDeliveredBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnSubmit.clipToOutline = true
            btnSkip.clipToOutline = true
        }
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        withBindingSafety(binding) {
            btnSubmit.setOnThrottleClickListener(resumedCompositeDisposable) {
                ratingSubject.onNext(ratingBar.rating.toInt())
                dismiss()
            }
            btnSkip.setOnThrottleClickListener(resumedCompositeDisposable) { dismiss() }

            tvTitle.setLocalizedTextByKey(ConfigStringKey.PACKAGE_DELIVERED_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.PACKAGE_DELIVERED_DESCRIPTION)

            btnSubmit.setLocalizedTextByKey(ConfigStringKey.SUBMIT)
            btnSkip.setLocalizedTextByKey(ConfigStringKey.SKIP)
        }
    }

    fun getRatingObservable(): Observable<Int> = ratingSubject
}