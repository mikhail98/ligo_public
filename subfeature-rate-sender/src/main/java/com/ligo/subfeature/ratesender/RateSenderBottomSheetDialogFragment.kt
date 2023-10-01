package com.ligo.subfeature.ratesender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.subfeature.ratesender.databinding.FragmentBottomSheetRateSenderBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class RateSenderBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<RateSenderBottomSheetDialogFragmentViewModel>() {

    companion object {

        const val TAG = "rate_sender"

        fun newInstance(): BottomSheetDialogFragment {
            return RateSenderBottomSheetDialogFragment()
        }
    }

    private val ratingSubject: Subject<Int> = PublishSubject.create<Int>().toSerialized()

    override val koinModule: Module = RateSenderModule
    override val viewModel: RateSenderBottomSheetDialogFragmentViewModel by inject()

    private var binding: FragmentBottomSheetRateSenderBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetRateSenderBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnSubmit.clipToOutline = true
            btnSkip.clipToOutline = true

            tvTitle.setLocalizedTextByKey(ConfigStringKey.RATE_SENDER_DIALOG_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.RATE_SENDER_DIALOG_DESCRIPTION)
            btnSkip.setLocalizedTextByKey(ConfigStringKey.SKIP)
            btnSubmit.setLocalizedTextByKey(ConfigStringKey.SUBMIT)
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            btnSubmit.setOnThrottleClickListener(startedCompositeDisposable) {
                ratingSubject.onNext(ratingBar.rating.toInt())
                dismiss()
            }
            btnSkip.setOnThrottleClickListener(startedCompositeDisposable) { dismiss() }
        }
    }

    fun getRatingObservable(): Observable<Int> = ratingSubject
}