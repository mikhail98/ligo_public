package com.ligo.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.ligo.common.BaseFragment
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.alert.showDialog
import com.ligo.common.withBindingSafety
import com.ligo.core.PermissionChecker
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.home.databinding.FragmentHomeBinding
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.PushArgs
import com.ligo.navigator.homeapi.HomeTarget
import com.ligo.navigator.homeapi.IHomeNavigator
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class HomeFragment : BaseFragment<HomeFragmentViewModel>() {
    companion object {

        fun newInstance(): Fragment {
            return HomeFragment()
        }
    }

    override val koinModule: Module = HomeModule
    override val viewModel by inject<HomeFragmentViewModel>()

    private val homeNavigator: IHomeNavigator by inject()

    private var binding: FragmentHomeBinding? = null

    private val onNavigationItemSelectedListener = OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.actionDelivery -> homeNavigator.open(HomeTarget.Delivery)
            R.id.actionHistory -> homeNavigator.open(HomeTarget.History)
            R.id.actionProfile -> homeNavigator.open(HomeTarget.Profile)
        }
        true
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.map { it.value }.all { it }) {
            initScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeNavigator.setup(this, R.id.fcvHome)
    }

    private fun handleLoading(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun handleUnreadChatCount(unreadChatCount: Int) {
        withBindingSafety(binding) {
            BottomMenuHelper.removeBadge(bottomNavigationView, R.id.actionProfile)
            if (unreadChatCount != 0) {
                BottomMenuHelper.showBadge(
                    requireContext(),
                    bottomNavigationView,
                    R.id.actionProfile,
                    unreadChatCount.toString()
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding?.bottomNavigationView?.setOnItemSelectedListener(onNavigationItemSelectedListener)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val permissionsResult = PermissionChecker.isLocationPermissionEnabled(requireContext())
        if (permissionsResult.first) {
            initScreen()
        } else {
            val locationText =
                getLocalizedStringByKey(ConfigStringKey.LOCATION_ALERT_SENDER_MESSAGE)

            val additionalText =
                getLocalizedStringByKey(ConfigStringKey.LOCATION_ALERT_MESSAGE_COMMON)

            requireContext().showDialog(
                getLocalizedStringByKey(ConfigStringKey.LOCATION_ALERT_MESSAGE_TITLE),
                locationText.plus(additionalText),
                getLocalizedStringByKey(ConfigStringKey.LOCATION_ALERT_OK_TEXT),
                getLocalizedStringByKey(ConfigStringKey.LOCATION_ALERT_CANCEL_TEXT)
            ) {
                requestMultiplePermissions.launch(permissionsResult.second)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleLoading)

        viewModel.getUnreadChatCountObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleUnreadChatCount)
    }

    private fun initScreen() {
        val pushArgs = getPushArgs()
        binding?.bottomNavigationView?.selectedItemId = when (pushArgs.homeTarget) {
            PushArgs.HomeTarget.DELIVERY -> R.id.actionDelivery
            PushArgs.HomeTarget.HISTORY -> R.id.actionHistory
            PushArgs.HomeTarget.PROFILE -> R.id.actionProfile
        }
        viewModel.requestNextStep(pushArgs.chatId)
        val newPushArgs = pushArgs.copy(chatId = null, homeTarget = PushArgs.HomeTarget.DELIVERY)
        activity?.intent?.putExtra(INavigator.EXTRA_PUSH_ARGS, newPushArgs)
    }
}