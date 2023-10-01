package com.ligo.subfeatureselectphotosource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey.FROM_CAMERA_BTN_TITLE
import com.ligo.data.model.ConfigStringKey.FROM_GALLERY_BTN_TITLE
import com.ligo.subfeature.selectphotosource.databinding.BottomSheetFragmentSelectPhotoSourceBinding
import com.ligo.tools.api.PickPhoto
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject

class SelectPhotoSourceBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<SelectPhotoSourceBottomSheetDialogFragmentViewModel>() {

    companion object {

        const val TAG = "SelectPhotoSourceBottomSheetFragment"

        fun newInstance(): SelectPhotoSourceBottomSheetDialogFragment {
            return SelectPhotoSourceBottomSheetDialogFragment()
        }
    }

    override val koinModule = SelectPhotoSourceModule

    override val viewModel by inject<SelectPhotoSourceBottomSheetDialogFragmentViewModel>()

    private var binding: BottomSheetFragmentSelectPhotoSourceBinding? = null

    private val pickPhotoSourceSubject: Subject<PickPhoto.Source> =
        PublishSubject.create<PickPhoto.Source>().toSerialized()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = BottomSheetFragmentSelectPhotoSourceBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            tvFromCamera.setLocalizedTextByKey(FROM_CAMERA_BTN_TITLE)
            tvFromGallery.setLocalizedTextByKey(FROM_GALLERY_BTN_TITLE)

            clFromCamera.setOnThrottleClickListener(startedCompositeDisposable) {
                pickPhotoSourceSubject.onNext(PickPhoto.Source.CAMERA)
                dismiss()
            }
            clFromGallery.setOnThrottleClickListener(startedCompositeDisposable) {
                pickPhotoSourceSubject.onNext(PickPhoto.Source.GALLERY)
                dismiss()
            }
        }
    }

    fun getOnSelectedPhotoSourceObservable(): Observable<PickPhoto.Source> {
        return pickPhotoSourceSubject
    }
}