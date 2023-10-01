package com.ligo.subfeature.createparcel.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.ligo.common.BaseBottomSheetDialogFragment
import com.ligo.common.ViewContainer
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.subfeature.createparcle.databinding.FragmentBottomSheetCurrencySelectionBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class CurrencySelectionBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<CurrencySelectionBottomSheetDialogFragmentViewModel>(),
    ViewContainer {

    companion object {
        const val TAG = "CurrencySelectionBottomSheetDialogFragment"

        private const val ARGS_DEFAULT_CURRENCY_CODE = "default_currency_code"

        fun newInstance(defaultCurrencyCode: String?): CurrencySelectionBottomSheetDialogFragment {
            return CurrencySelectionBottomSheetDialogFragment().apply {
                arguments = bundleOf(ARGS_DEFAULT_CURRENCY_CODE to defaultCurrencyCode)
            }
        }
    }

    private val currencySubject: Subject<String> = PublishSubject.create<String>().toSerialized()

    override val koinModule: Module = CurrencySelectionModule
    override val viewModel: CurrencySelectionBottomSheetDialogFragmentViewModel by inject()

    private var binding: FragmentBottomSheetCurrencySelectionBinding? = null

    private var adapter: CurrencySelectionAdapter = CurrencySelectionAdapter(localizationManager)
    private var selectedCurrencyCode: String = ""

    private val defaultCurrencyCode: String by lazy {
        arguments?.getString(ARGS_DEFAULT_CURRENCY_CODE).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedCurrencyCode = defaultCurrencyCode
        viewModel.getCurrencyItemUiListObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) {
                adapter.updateCurrencyList(it)
            }

        viewModel.fetchCurrencyItemUiList(defaultCurrencyCode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomSheetCurrencySelectionBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            rvCurrency.adapter = adapter
            btnBack.clipToOutline = true
            btnSubmit.clipToOutline = true
            tvTitle.setLocalizedTextByKey(ConfigStringKey.SELECT_CURRENCY)
            btnBack.setLocalizedTextByKey(ConfigStringKey.BACK)
            btnSubmit.setLocalizedTextByKey(ConfigStringKey.SUBMIT)
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        adapter.getOnItemClickObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable) {
                adapter.selectCurrency(it.currency)
                selectedCurrencyCode = it.currency.code
            }
        withBindingSafety(binding) {
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) { dismiss() }
            btnSubmit.setOnThrottleClickListener(startedCompositeDisposable) {
                currencySubject.onNext(selectedCurrencyCode)
                dismiss()
            }
        }
    }

    fun getCurrencyObservable(): Observable<String> = currencySubject
}