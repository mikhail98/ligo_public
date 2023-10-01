package com.ligo.subfeature.parceltype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.ViewContainer
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.subfeature.parceltype.databinding.FragmentBottomSheetParcelTypeInfoBinding
import com.ligo.subfeature.parceltype.recycler.ParcelTypeAdapter
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ParcelTypeInfoBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<ParcelTypeInfoBottomSheetDialogFragmentViewModel>(),
    ViewContainer {

    companion object {

        const val TAG = "SizeBottomSheetDialog"

        fun newInstance(): BottomSheetDialogFragment {
            return ParcelTypeInfoBottomSheetDialogFragment()
        }
    }

    override val koinModule: Module = ParcelTypeInfoModule
    override val viewModel: ParcelTypeInfoBottomSheetDialogFragmentViewModel by inject()

    private var binding: FragmentBottomSheetParcelTypeInfoBinding? = null

    private val adapter by lazy { ParcelTypeAdapter(localizationManager, true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getParcelTypeUiListObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) { adapter.updateItemList(it) }

        viewModel.fetchParcelTypeUiList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetParcelTypeInfoBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(ConfigStringKey.PARCEL_TYPE_DIALOG_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.PARCEL_TYPE_DIALOG_DESCRIPTION)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding?.rvParcelTypes?.adapter = adapter
    }
}