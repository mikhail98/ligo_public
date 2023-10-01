package com.ligo.subfeature.createparcel.parceldetails

import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.ViewContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.button.ActionButton
import com.ligo.common.ui.edittext.listeners.TextListener
import com.ligo.common.withBindingSafety
import com.ligo.core.loadImageWithGlide
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.DefaultParcelType
import com.ligo.subfeature.createparcle.databinding.FragmentBottomSheetParcelDetailsBinding
import com.ligo.subfeature.parceltype.ParcelTypeBottomSheetDialogFragment
import com.ligo.subfeatureselectphotosource.SelectPhotoSourceBottomSheetDialogFragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional

class ParcelDetailsBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<ParcelDetailsBottomSheetDialogFragmentViewModel>(),
    ViewContainer {

    companion object {
        const val TAG = "ParcelDetailsBottomSheetDialogFragment"

        private const val ARGS_PARCEL_DETAILS = "ARGS_PARCEL_DETAILS"

        fun newInstance(details: ParcelDetails): ParcelDetailsBottomSheetDialogFragment {
            return ParcelDetailsBottomSheetDialogFragment().apply {
                arguments = bundleOf(ARGS_PARCEL_DETAILS to details)
            }
        }
    }

    private val parcelDetailsSubject: Subject<Optional<ParcelDetails>> =
        PublishSubject.create<Optional<ParcelDetails>>().toSerialized()

    override val koinModule: Module = ParcelDetailsModule
    override val viewModel: ParcelDetailsBottomSheetDialogFragmentViewModel by inject()

    private var binding: FragmentBottomSheetParcelDetailsBinding? = null

    private val weightTextListener by lazy { TextListener(viewModel::setWeight) }

    private val parcelDetails: ParcelDetails? by lazy { arguments?.getParcelable(ARGS_PARCEL_DETAILS) }

    override fun onChildFragmentStarted(f: Fragment) {
        when (f) {
            is ParcelTypeBottomSheetDialogFragment -> {
                f.getParcelTypeListObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::setTypeList)
            }

            is SelectPhotoSourceBottomSheetDialogFragment -> {
                f.getOnSelectedPhotoSourceObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::pickPhoto)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetParcelDetailsBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnSave.clipToOutline = true
            btnBack.clipToOutline = true
            tivWeight.setLocalizedHintByKey(ConfigStringKey.WEIGHT)
            tivParcelType.setLocalizedHintByKey(ConfigStringKey.PARCEL_TYPE)
            btnSave.setLocalizedTextByKey(ConfigStringKey.SAVE)
            btnBack.setLocalizedTextByKey(ConfigStringKey.BACK)
            tvAttachPhoto.setLocalizedTextByKey(ConfigStringKey.TAKE_PHOTO_OF_PARCEL)
            tvTitle.setLocalizedTextByKey(ConfigStringKey.PACKAGE_INFORMATION_TITLE)
            tvSubtitle.setLocalizedTextByKey(ConfigStringKey.PACKAGE_INFORMATION_DESCRIPTION)
            tvPhotoHint.setLocalizedTextByKey(ConfigStringKey.TAKE_PHOTO_OF_PARCEL_HINT)

            tivWeight.getEditText().apply {
                addTextChangedListener(weightTextListener)
                inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                keyListener = DigitsKeyListener.getInstance("0123456789")
            }
        }
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getParcelDetailsObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleParcelDetails)
        viewModel.init(parcelDetails)
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            btnSave.setOnThrottleClickListener(startedCompositeDisposable) {
                parcelDetailsSubject.onNext(Optional.ofNullable(parcelDetails))
                dismiss()
            }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) { dismiss() }
            btnDeletePhoto.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.setPhoto(null)
            }

            clPickPhoto.setOnThrottleClickListener(startedCompositeDisposable) {
                SelectPhotoSourceBottomSheetDialogFragment.newInstance()
                    .show(childFragmentManager, SelectPhotoSourceBottomSheetDialogFragment.TAG)
            }

            tivParcelType.setOnTivThrottleClickListener(startedCompositeDisposable) {
                ParcelTypeBottomSheetDialogFragment.newInstance(viewModel.parcelDetails.typeList)
                    .show(childFragmentManager, ParcelTypeBottomSheetDialogFragment.TAG)
            }
        }
    }

    private fun handleParcelDetails(parcelDetails: ParcelDetails) {
        this.parcelDetails?.weight = parcelDetails.weight
        this.parcelDetails?.typeList = parcelDetails.typeList
        this.parcelDetails?.parcelPhotoUrl = parcelDetails.parcelPhotoUrl
        setWeight(parcelDetails.weight)
        setTypeList(parcelDetails.typeList)
        setPhoto(parcelDetails.parcelPhotoUrl)
        setSaveButtonEnabled(parcelDetails.isFilled)
    }

    private fun setSaveButtonEnabled(isFilled: Boolean) {
        binding?.btnSave?.state = if (isFilled) {
            ActionButton.State.PRIMARY
        } else {
            ActionButton.State.INACTIVE
        }
    }

    private fun setWeight(reward: Int) {
        withBindingSafety(binding) {
            tivWeight.getEditText().removeTextChangedListener(weightTextListener)
            if (reward != -1) {
                val text = reward.toString()
                tivWeight.highlight(true)
                tivWeight.setText(text)
                tivWeight.getEditText().setSelection(text.length)
            } else {
                tivWeight.highlight(false)
                tivWeight.setText(null)
                tivWeight.clearFocus()
            }
            tivWeight.getEditText().addTextChangedListener(weightTextListener)
        }
    }

    private fun setTypeList(typeList: List<String>) {
        withBindingSafety(binding) {
            if (typeList.isNotEmpty()) {
                tivParcelType.highlight(true)
                tivParcelType.setText(DefaultParcelType.stringify(typeList))
            } else {
                tivParcelType.highlight(false)
                tivParcelType.setText(null)
                tivParcelType.clearFocus()
            }
        }
    }

    private fun setPhoto(photoUrl: String?) {
        withBindingSafety(binding) {
            clPickPhoto.isVisible = photoUrl == null
            clPickedPhoto.isVisible = photoUrl != null

            tvImageTitle.text = photoUrl.orEmpty().hashCode().toString().plus(".JPEG")
            ivPickedPhoto.loadImageWithGlide(photoUrl.orEmpty())
        }
    }

    fun getOnParcelDetailsObservable(): Observable<Optional<ParcelDetails>> = parcelDetailsSubject
}