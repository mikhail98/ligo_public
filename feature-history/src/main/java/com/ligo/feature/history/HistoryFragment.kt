package com.ligo.feature.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.model.HistoryUiModel
import com.ligo.common.model.StatusUiModel.ParcelAccepted
import com.ligo.common.model.StatusUiModel.ParcelCanceled
import com.ligo.common.model.StatusUiModel.ParcelCreated
import com.ligo.common.model.StatusUiModel.ParcelDelivered
import com.ligo.common.model.StatusUiModel.ParcelPicked
import com.ligo.common.model.StatusUiModel.ParcelRejected
import com.ligo.common.model.StatusUiModel.TripActive
import com.ligo.common.model.StatusUiModel.TripScheduled
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey.EMPTY_HISTORY_DESCRIPTION
import com.ligo.data.model.ConfigStringKey.EMPTY_HISTORY_TITLE
import com.ligo.data.model.ConfigStringKey.TRIPS_HISTORY_TITLE
import com.ligo.feature.history.databinding.FragmentHistoryBinding
import com.ligo.feature.history.recycler.HistoryAdapter
import com.ligo.navigator.api.Target.DriverTrip
import com.ligo.navigator.api.Target.SearchForDriver
import com.ligo.navigator.api.Target.SenderParcel
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class HistoryFragment : BaseFragment<HistoryFragmentViewModel>() {

    companion object {
        const val TAG = "history"

        fun newInstance(): Fragment {
            return HistoryFragment()
        }
    }

    override val koinModule: Module = HistoryModule
    override val viewModel by inject<HistoryFragmentViewModel>()

    private var binding: FragmentHistoryBinding? = null

    private val historyAdapter by lazy { HistoryAdapter(localizationManager) }

    override fun onStart() {
        super.onStart()
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleLoading)

        viewModel.getHistoryUiModelListObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleHistoryItems)

        historyAdapter.getOnItemClickedObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleItemClicked)
    }

    private fun handleLoading(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        binding?.rvHistory?.adapter = historyAdapter
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(TRIPS_HISTORY_TITLE)
            tvNoItemTitle.setLocalizedTextByKey(EMPTY_HISTORY_TITLE)
            tvNoItemDescription.setLocalizedTextByKey(EMPTY_HISTORY_DESCRIPTION)
        }
    }

    private fun handleHistoryItems(items: List<HistoryUiModel>) {
        val isHistoryEmpty = items.isEmpty()
        withBindingSafety(binding) {
            clNoItem.setVisibilityWithAlpha(isHistoryEmpty)
            rvHistory.setVisibilityWithAlpha(!isHistoryEmpty)
            if (!isHistoryEmpty) historyAdapter.setItems(items)
        }
    }

    private fun handleItemClicked(data: HistoryUiModel) {
        val trip = data.trip
        val parcel = data.parcel
        when (data.uiStatus) {
            ParcelPicked, ParcelAccepted -> parcel?.let { navigator.open(SenderParcel(it._id)) }
            ParcelCreated -> parcel?.let { navigator.open(SearchForDriver(parcel._id)) }
            ParcelCanceled, ParcelDelivered -> parcel?.let {
                ParcelInfoBottomSheetDialogFragment.newInstance(it._id)
                    .show(childFragmentManager, ParcelInfoBottomSheetDialogFragment.TAG)
            }

            ParcelRejected -> parcel?.let {
                RejectedParcelInfoBottomSheetDialogFragment.newInstance(it._id)
                    .show(childFragmentManager, RejectedParcelInfoBottomSheetDialogFragment.TAG)
            }

            TripActive, TripScheduled -> trip?.let { navigator.open(DriverTrip(it._id)) }
            else -> Unit
        }
    }
}