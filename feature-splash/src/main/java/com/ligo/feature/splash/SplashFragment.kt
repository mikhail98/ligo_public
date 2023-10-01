package com.ligo.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.alert.showUpdateAppDialog
import com.ligo.core.BuildConfig
import com.ligo.feature.splash.databinding.FragmentSplashBinding
import com.ligo.navigator.api.Target
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class SplashFragment : BaseFragment<SplashFragmentViewModel>() {

    companion object {

        fun newInstance(): Fragment {
            return SplashFragment()
        }
    }

    override val koinModule: Module = SplashModule
    override val viewModel by inject<SplashFragmentViewModel>()

    private var binding: FragmentSplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getOnUpdateAppObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::showUpdateAppDialog)

        viewModel.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding?.root
    }

    private fun showUpdateAppDialog(data: Pair<Boolean, Boolean>) {
        if (data.first) {
            activity?.apply {
                if (data.second) {
                    viewModel.logout()
                }
                showUpdateAppDialog(localizationManager, !data.second) {
                    navigator.open(Target.BrowserApp(BuildConfig.APP_URL))
                }
            }
        }
    }
}