package com.ligo.subfeature.parceltype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.model.ParcelTypeUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.subfeature.parceltype.databinding.FragmentBottomSheetParcelTypeBinding
import com.ligo.subfeature.parceltype.recycler.ParcelTypeAdapter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ParcelTypeBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<ParcelTypeBottomSheetDialogFragmentViewModel>() {

    companion object {
        const val TAG = "parcel_type"

        private const val ARGS_SELECTED_PARCEL_TYPES = "selected_parcel_types"

        fun newInstance(selectedParcelTypeList: List<String>?): ParcelTypeBottomSheetDialogFragment {
            return ParcelTypeBottomSheetDialogFragment().apply {
                val arrayList = ArrayList(selectedParcelTypeList ?: emptyList())
                arguments = bundleOf(ARGS_SELECTED_PARCEL_TYPES to arrayList)
            }
        }
    }

    private val parcelTypeListSubject: Subject<List<String>> =
        PublishSubject.create<List<String>>().toSerialized()

    override val koinModule: Module = ParcelTypeModule
    override val viewModel: ParcelTypeBottomSheetDialogFragmentViewModel by inject()

    private var binding: FragmentBottomSheetParcelTypeBinding? = null

    private val selectedParcelTypeSet: MutableSet<String> = mutableSetOf()

    private val adapter by lazy { ParcelTypeAdapter(localizationManager, false) }

    private val selectedParcelTypeList: List<String> by lazy {
        arguments?.getStringArrayList(ARGS_SELECTED_PARCEL_TYPES) ?: emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getParcelTypeUiListObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) { adapter.updateItemList(it) }

        viewModel.fetchParcelTypeUiList(selectedParcelTypeList)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetParcelTypeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedParcelTypeSet.addAll(selectedParcelTypeList)
        withBindingSafety(binding) {
            rvPackageTypes.adapter = adapter

            btnBack.clipToOutline = true
            btnSubmit.clipToOutline = true
        }
    }

    override fun onResume() {
        super.onResume()
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(ConfigStringKey.PARCEL_TYPE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.SELECT_PARCEL_TYPE)
            btnBack.setLocalizedTextByKey(ConfigStringKey.BACK)
            btnSubmit.setLocalizedTextByKey(ConfigStringKey.SUBMIT)
        }
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) { dismiss() }
            btnSubmit.setOnThrottleClickListener(startedCompositeDisposable) {
                if (selectedParcelTypeSet.isEmpty()) return@setOnThrottleClickListener
                parcelTypeListSubject.onNext(selectedParcelTypeSet.toList())
                dismiss()
            }
        }

        adapter.getOnItemClickObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleItemClicked)
    }

    private fun handleItemClicked(item: ParcelTypeUiModel) {
        adapter.updateItem(item)
        if (item.isChecked) {
            selectedParcelTypeSet.add(item.type)
        } else {
            selectedParcelTypeSet.remove(item.type)
        }
    }

    fun getParcelTypeListObservable(): Observable<List<String>> = parcelTypeListSubject
}