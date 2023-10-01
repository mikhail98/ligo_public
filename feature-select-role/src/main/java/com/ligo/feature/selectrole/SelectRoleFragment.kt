package com.ligo.feature.selectrole

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.R
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.UserRole
import com.ligo.feature.selectrole.databinding.FragmentSelectRoleBinding
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class SelectRoleFragment : BaseFragment<SelectRoleFragmentViewModel>() {

    companion object {
        const val TAG_BACKSTACK = "select_role"

        fun newInstance(): Fragment {
            return SelectRoleFragment()
        }
    }

    override val koinModule: Module = SelectRoleModule
    override val viewModel by inject<SelectRoleFragmentViewModel>()
    private var binding: FragmentSelectRoleBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoadingState)
        viewModel.getUserRoleObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleUserRole)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun handleUserRole(userRole: UserRole) {
        val bgActive = R.drawable.bg_input_filled
        val bgInactive = R.drawable.bg_input
        when (userRole) {
            UserRole.DRIVER -> {
                binding?.clRoleDriver?.setBackgroundResource(bgActive)
                binding?.clRoleSender?.setBackgroundResource(bgInactive)
            }

            UserRole.SENDER -> {
                binding?.clRoleDriver?.setBackgroundResource(bgInactive)
                binding?.clRoleSender?.setBackgroundResource(bgActive)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSelectRoleBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            clRoleSender.clipToOutline = true
            clRoleDriver.clipToOutline = true
            btnNext.clipToOutline = true

            btnNext.setLocalizedTextByKey(ConfigStringKey.NEXT)
            tvSenderTitle.setLocalizedTextByKey(ConfigStringKey.SENDER)
            tvDriverTitle.setLocalizedTextByKey(ConfigStringKey.DRIVER)
            tvHint.setLocalizedTextByKey(ConfigStringKey.SELECT_ROLE_HINT)
            tvTitle.setLocalizedTextByKey(ConfigStringKey.SELECT_ROLE_TITLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.SELECT_ROLE_DESCRIPTION)
            tvDriverDescription.setLocalizedTextByKey(ConfigStringKey.DRIVER_DESCRIPTION)
            tvSenderDescription.setLocalizedTextByKey(ConfigStringKey.SENDER_DESCRIPTION)
        }
        viewModel.setUserRole(UserRole.SENDER)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(SelectRoleFragment::class.java)
            }
            clRoleSender.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.setUserRole(UserRole.SENDER)
            }
            clRoleDriver.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.setUserRole(UserRole.DRIVER)
            }
            btnNext.setOnThrottleClickListener(startedCompositeDisposable) { viewModel.register() }
        }
    }
}