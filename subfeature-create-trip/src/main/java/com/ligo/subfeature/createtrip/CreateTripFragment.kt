package com.ligo.subfeature.createtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.button.ActionButton
import com.ligo.common.withBindingSafety
import com.ligo.core.PermissionsManager
import com.ligo.data.model.ConfigStringKey
import com.ligo.navigator.api.Target
import com.ligo.subfeature.createtrip.databinding.FragmentCreateTripBinding
import com.ligo.tools.api.SearchPlaceRequest
import com.ligo.tools.api.SearchPlaceResult
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class CreateTripFragment : BaseFragment<CreateTripFragmentViewModel>() {

    companion object {
        private const val CREATE_CLICK_THROTTLE = 500L

        fun newInstance(): Fragment {
            return CreateTripFragment()
        }
    }

    override val koinModule: Module = CreateTripModule
    override val viewModel by inject<CreateTripFragmentViewModel>()

    override var registerBackpressureCallback: Boolean = false

    private var binding: FragmentCreateTripBinding? = null

    private val permissionsManager by lazy { PermissionsManager(requireActivity()) }

    private var isCreationEnabled = false
    private var createButtonClickTime = System.currentTimeMillis()

    override fun onChildFragmentStarted(f: Fragment) {
        when (f) {
            is DatePickerBottomSheetDialogFragment -> {
                f.getDetailedDateObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::createTrip)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoadingState)

        viewModel.getButtonStateObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::updateNextBtnState)

        viewModel.getClearAllFieldsObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) { clearAllFields() }

        viewModel.getOnPlacePickedObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handlePlacePicked)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCreateTripBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(ConfigStringKey.CREATE_TRIP_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.CREATE_TRIP_DESCRIPTION)
            btnCreateTrip.setLocalizedTextByKey(ConfigStringKey.CREATE_TRIP)
            btnPlanTrip.setLocalizedTextByKey(ConfigStringKey.PLAN_TRIP)
            tivFrom.setLocalizedHintByKey(ConfigStringKey.FROM)
            tivTo.setLocalizedHintByKey(ConfigStringKey.TO)
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            btnCreateTrip.clipToOutline = true
            btnPlanTrip.clipToOutline = true

            if (tivFrom.getText().isNotEmpty()) {
                tivFrom.highlight()
            } else {
                viewModel.startPoint?.address?.let { cityName ->
                    tivFrom.setText(cityName)
                    tivFrom.highlight()
                    viewModel.checkAllFieldsFilled()
                }
            }
            if (tivTo.getText().isNotEmpty()) {
                tivTo.highlight()
            } else {
                viewModel.endPoint?.address?.let { cityName ->
                    tivTo.setText(cityName)
                    tivTo.highlight()
                    viewModel.checkAllFieldsFilled()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        withBindingSafety(binding) {
            btnPlanTrip.setOnThrottleClickListener(startedCompositeDisposable) {
                if (isCreationEnabled && getClickEnabled()) {
                    createButtonClickTime = System.currentTimeMillis()
                    showDatePicker()
                }
            }
            btnCreateTrip.setOnThrottleClickListener(startedCompositeDisposable) {
                if (isCreationEnabled && getClickEnabled()) {
                    createButtonClickTime = System.currentTimeMillis()
                    if (permissionsManager.requestLocationPermissions()) {
                        viewModel.createTrip(date = null)
                    }
                }
            }
            tivFrom.setOnTivThrottleClickListener(startedCompositeDisposable) {
                navigator.open(Target.SearchPlace(SearchPlaceRequest.Origin.START_TRIP_FROM))
            }
            tivTo.setOnTivThrottleClickListener(startedCompositeDisposable) {
                navigator.open(Target.SearchPlace(SearchPlaceRequest.Origin.START_TRIP_TO))
            }
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun updateNextBtnState(isActive: Boolean) {
        isCreationEnabled = isActive
        withBindingSafety(binding) {
            if (isCreationEnabled) {
                btnPlanTrip.state = ActionButton.State.SECONDARY
                btnCreateTrip.state = ActionButton.State.PRIMARY
            } else {
                btnPlanTrip.state = ActionButton.State.INACTIVE
                btnCreateTrip.state = ActionButton.State.INACTIVE
            }
        }
    }

    private fun getClickEnabled(): Boolean {
        return System.currentTimeMillis() - createButtonClickTime > CREATE_CLICK_THROTTLE
    }

    private fun handlePlacePicked(spResult: SearchPlaceResult) {
        val location = spResult.location
        val name = location?.fullName

        when (val origin = spResult.origin) {
            SearchPlaceRequest.Origin.START_TRIP_FROM -> {
                binding?.tivFrom?.setText(name)
                binding?.tivFrom?.highlight(location != null)
                viewModel.setPoint(location, origin)
            }

            SearchPlaceRequest.Origin.START_TRIP_TO -> {
                binding?.tivTo?.setText(name)
                binding?.tivTo?.highlight(location != null)
                viewModel.setPoint(location, origin)
            }

            else -> Unit
        }
    }

    private fun clearAllFields() {
        withBindingSafety(binding) {
            tivFrom.setText(null)
            tivFrom.highlight(false)

            tivTo.setText(null)
            tivTo.highlight(false)
        }
    }

    private fun showDatePicker() {
        DatePickerBottomSheetDialogFragment.newInstance(
            ConfigStringKey.PLAN_TRIP_DATE_PICKER_TITLE,
            ConfigStringKey.PLAN_TRIP_DATE_PICKER_DESCRIPTION
        )
            .show(childFragmentManager, DatePickerBottomSheetDialogFragment.TAG)
    }
}