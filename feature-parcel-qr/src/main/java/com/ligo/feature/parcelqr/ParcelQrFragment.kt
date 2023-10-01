package com.ligo.feature.parcelqr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.parcelqr.databinding.FragmentParcelQrBinding
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ParcelQrFragment : BaseFragment<ParcelQrFragmentViewModel>() {

    companion object {

        const val TAG_BACKSTACK = "ParcelQrFragment"
        private const val ARGS_DATA = "data"

        fun newInstance(data: String): Fragment {
            return ParcelQrFragment().apply {
                arguments = bundleOf(ARGS_DATA to data)
            }
        }
    }

    override val koinModule: Module = ParcelQrModule
    override val viewModel by inject<ParcelQrFragmentViewModel>()

    private var binding: FragmentParcelQrBinding? = null

    private val data by lazy { arguments?.getString(ARGS_DATA).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentParcelQrBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            ivQr.setImageBitmap(viewModel.getQr(data))
            tvHeader.setLocalizedTextByKey(ConfigStringKey.QR_CODE)
            tvTitle.setLocalizedTextByKey(ConfigStringKey.SHOW_QR_TO_DRIVER)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.SHOW_QR_TO_DRIVER_DESCRIPTION)
        }

        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        binding?.btnBack?.setOnThrottleClickListener(startedCompositeDisposable) {
            navigator.closeFragment(ParcelQrFragment::class.java)
        }
    }
}