package com.ligo.subfeature.createparcel

import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.button.ActionButton
import com.ligo.common.ui.edittext.listeners.TextListener
import com.ligo.common.withBindingSafety
import com.ligo.core.loadImageWithGlide
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.DefaultParcelType
import com.ligo.data.model.Location
import com.ligo.google.api.RemoteConfigCurrency
import com.ligo.navigator.api.Target
import com.ligo.subfeature.createparcel.currency.CurrencySelectionBottomSheetDialogFragment
import com.ligo.subfeature.createparcel.parceldetails.ParcelDetailsBottomSheetDialogFragment
import com.ligo.subfeature.createparcle.databinding.FragmentCreateParcelBinding
import com.ligo.tools.api.SearchPlaceRequest.Origin.SEND_PARCEL_FROM
import com.ligo.tools.api.SearchPlaceRequest.Origin.SEND_PARCEL_TO
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class CreateParcelFragment : BaseFragment<CreateParcelFragmentViewModel>() {

    companion object {
        fun newInstance(): Fragment {
            return CreateParcelFragment()
        }
    }

    override val koinModule: Module = CreateParcelModule
    override val viewModel by inject<CreateParcelFragmentViewModel>()

    override var registerBackpressureCallback: Boolean = false

    private var binding: FragmentCreateParcelBinding? = null

    private val rewardTextListener by lazy { TextListener(viewModel::setReward) }

    override fun onChildFragmentStarted(f: Fragment) {
        when (f) {
            is CurrencySelectionBottomSheetDialogFragment -> {
                f.getCurrencyObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::setCurrencyCode)
            }

            is ParcelDetailsBottomSheetDialogFragment -> {
                f.getOnParcelDetailsObservable()
                    .subscribeAndDisposeAt(
                        f.startedCompositeDisposable,
                        viewModel::setParcelDetails
                    )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoadingState)

        viewModel.getCreateParcelDataObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleCreateParcelData)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCreateParcelBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(ConfigStringKey.SEND_PARCEL_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.SEND_PARCEL_DESCRIPTION)
            btnCreateParcel.setLocalizedTextByKey(ConfigStringKey.SEARCH_FOR_DRIVER)
            tvPickParcelDetails.setLocalizedTextByKey(ConfigStringKey.PACKAGE_INFORMATION_TITLE)
            tvParcelDetailsPicked.setLocalizedTextByKey(ConfigStringKey.PACKAGE_INFORMATION_TITLE)
            tivFrom.setLocalizedHintByKey(ConfigStringKey.FROM)
            tivTo.setLocalizedHintByKey(ConfigStringKey.TO)
            tivReward.setLocalizedHintByKey(ConfigStringKey.DELIVERY_REWARD)
            tivReward.getEditText().apply {
                addTextChangedListener(rewardTextListener)
                inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                keyListener = DigitsKeyListener.getInstance("0123456789")
            }

            btnCreateParcel.clipToOutline = true
        }

        viewModel.reset()

        return binding?.root
    }

    override fun onStart() {
        super.onStart()

        withBindingSafety(binding) {
            btnCreateParcel.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.createParcel()
            }

            clCurrency.setOnThrottleClickListener(startedCompositeDisposable) {
                CurrencySelectionBottomSheetDialogFragment.newInstance(viewModel.createParcelData.currencyCode)
                    .show(childFragmentManager, CurrencySelectionBottomSheetDialogFragment.TAG)
            }

            tivFrom.setOnTivThrottleClickListener(startedCompositeDisposable) {
                navigator.open(Target.SearchPlace(SEND_PARCEL_FROM))
            }

            tivTo.setOnTivThrottleClickListener(startedCompositeDisposable) {
                navigator.open(Target.SearchPlace(SEND_PARCEL_TO))
            }

            clParcelDetails.setOnThrottleClickListener(startedCompositeDisposable) {
                ParcelDetailsBottomSheetDialogFragment.newInstance(viewModel.createParcelData.parcelDetails)
                    .show(childFragmentManager, ParcelDetailsBottomSheetDialogFragment.TAG)
            }
        }
    }

    private fun handleCreateParcelData(createData: CreateParcelData) {
        setStartPoint(createData.startPoint)
        setEndPoint(createData.endPoint)
        setCurrencyCode(createData.currencyCode)
        setReward(createData.reward)
        setParcelDetails(createData.weight, createData.types, createData.parcelPhotoUrl)
        setCreateParcelButtonState(createData.isFilled)
    }

    private fun setStartPoint(startPoint: Location?) {
        withBindingSafety(binding) {
            if (startPoint != null) {
                tivFrom.setText(startPoint.cityName ?: startPoint.address)
                tivFrom.highlight()
            } else {
                tivFrom.setText(null)
                tivFrom.highlight(false)
            }
        }
    }

    private fun setEndPoint(endPoint: Location?) {
        withBindingSafety(binding) {
            if (endPoint != null) {
                tivTo.setText(endPoint.cityName ?: endPoint.address)
                tivTo.highlight()
            } else {
                tivTo.setText(null)
                tivTo.highlight(false)
            }
        }
    }

    private fun setCurrencyCode(currencyCode: String) {
        withBindingSafety(binding) {
            tvCurrency.text = currencyCode
            ivCurrency.setBackgroundResource(RemoteConfigCurrency.fromCode(currencyCode).iconRes)
        }
    }

    private fun setReward(reward: Int) {
        withBindingSafety(binding) {
            tivReward.getEditText().removeTextChangedListener(rewardTextListener)
            if (reward != -1) {
                val text = reward.toString()
                viewRewardDivider.setBackgroundResource(com.ligo.core.R.color.accent)
                tivReward.highlight(true)
                tivReward.setText(text)
                tivReward.getEditText().setSelection(text.length)
            } else {
                viewRewardDivider.setBackgroundResource(com.ligo.core.R.color.gray_40)
                tivReward.highlight(false)
                tivReward.setText(null)
                tivReward.clearFocus()
            }
            tivReward.getEditText().addTextChangedListener(rewardTextListener)
        }
    }

    private fun setParcelDetails(weight: Int, typeList: List<String>, photoUrl: String?) {
        if (weight != -1 && typeList.isNotEmpty() && photoUrl != null) {
            binding?.clPickParcelDetails?.isVisible = false
            binding?.clParcelDetailsPicked?.isVisible = true
        } else {
            binding?.clPickParcelDetails?.isVisible = true
            binding?.clParcelDetailsPicked?.isVisible = false
        }

        withBindingSafety(binding) {
            ivPickedPhoto.loadImageWithGlide(photoUrl.orEmpty())
            val text = DefaultParcelType.stringify(typeList) + " - $weight kg"
            tvParcelDetailsDescriptionPicked.text = text
        }
    }

    private fun setCreateParcelButtonState(isEnabled: Boolean) {
        withBindingSafety(binding) {
            btnCreateParcel.state = if (isEnabled) {
                ActionButton.State.PRIMARY
            } else {
                ActionButton.State.INACTIVE
            }
        }
    }
}