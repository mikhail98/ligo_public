package com.ligo.feature.rejectparcel

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.REJECT_PARCEL_SELECT_REJECTION_REASON
import com.ligo.data.model.ConfigStringKey.WAIT_UNTIL_PHOTO_UPLOAD_FINISHED
import com.ligo.feature.rejectparcel.databinding.FragmentRejectParcelBinding
import com.ligo.tools.api.PickPhoto
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class RejectParcelFragment : BaseFragment<RejectParcelFragmentViewModel>() {

    companion object {
        const val TAG_BACKSTACK = "reject_parcel"

        private const val ARGS_PARCEL_ID = "RejectParcelFragment.parcelId"

        fun newInstance(parcelId: String): Fragment {
            return RejectParcelFragment().apply {
                arguments = bundleOf(ARGS_PARCEL_ID to parcelId)
            }
        }
    }

    override val koinModule: Module = RejectParcelModule
    override val viewModel by inject<RejectParcelFragmentViewModel>()

    private var binding: FragmentRejectParcelBinding? = null

    private val adapter by lazy {
        RejectParcelReasonAdapter(RejectReasonUi.getReasons(), localizationManager)
    }

    private var isPhotoLoading: Boolean = false

    private val parcelId by lazy { arguments?.getString(ARGS_PARCEL_ID).orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoadingState)

        viewModel.getPhotoUploadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable) {
                val photo = it.first
                val isLoading = it.second
                binding?.clPickupPhoto?.isVisible = photo == null
                binding?.clPickedPhoto?.isVisible = photo != null
                if (photo != null && photo.origin == PickPhoto.Origin.PARCEL_REJECTION) {
                    handlePhotoLoading(photo.uri, isLoading)
                }
            }
    }

    private fun handlePhotoLoading(uri: Uri, isLoading: Boolean) {
        isPhotoLoading = isLoading
        withBindingSafety(binding) {
            ivRejectPhoto.setImageURI(uri)
            ivDeletePhoto.isVisible = !isPhotoLoading
            progressPhoto.isVisible = isPhotoLoading
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentRejectParcelBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            btnReject.clipToOutline = true
            rvRejectReason.adapter = adapter

            tvTitle.setLocalizedTextByKey(ConfigStringKey.REJECT_PARCEL_PICKUP_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.REJECT_PARCEL_PICKUP_DESCRIPTION)
            btnBack.setLocalizedTextByKey(ConfigStringKey.BACK)
            btnReject.setLocalizedTextByKey(ConfigStringKey.REJECT)
            tvAttachPhoto.setLocalizedTextByKey(ConfigStringKey.REJECT_PARCEL_ATTACH_PHOTO)
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()

        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            ivDeletePhoto.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.removePickedPhoto()
            }
            clPickupPhoto.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.takePhotoForParcelRejection()
            }
            btnReject.setOnThrottleClickListener(startedCompositeDisposable) {
                val reason = adapter.items.find { it.isChecked }?.reason
                if (reason == null) {
                    showSnackByStringKey(REJECT_PARCEL_SELECT_REJECTION_REASON)
                    return@setOnThrottleClickListener
                }
                if (isPhotoLoading) {
                    showSnackByStringKey(WAIT_UNTIL_PHOTO_UPLOAD_FINISHED)
                    return@setOnThrottleClickListener
                }
                viewModel.rejectParcel(parcelId, reason)
            }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(RejectParcelFragment::class.java)
            }
        }

        adapter.getOnItemClickObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, adapter::updateReasonsList)
    }
}