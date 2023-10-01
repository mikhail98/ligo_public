package com.ligo.feature.aboutapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.core.BuildConfig
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.aboutapp.databinding.FragmentAboutAppBinding
import com.ligo.navigator.api.Target
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class AboutAppFragment : BaseFragment<AboutAppFragmentViewModel>() {

    companion object {
        const val TAG_BACKSTACK = "about_app"

        fun newInstance(): Fragment {
            return AboutAppFragment()
        }
    }

    override val koinModule: Module = AboutAppModule
    override val viewModel by inject<AboutAppFragmentViewModel>()

    private var binding: FragmentAboutAppBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAboutAppBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            val text = BuildConfig.VERSION_NAME
            tvAppVersionName.text = if (BuildConfig.SANDBOX) {
                text + " " + BuildConfig.FLAVOR.uppercase()
            } else {
                text
            }
            tvTitle.setLocalizedTextByKey(ConfigStringKey.ABOUT_APP)
            tvRateUs.setLocalizedTextByKey(ConfigStringKey.RATE_US)
            tvOpenTrip.setLocalizedTextByKey(ConfigStringKey.OPEN_TRIP_SCREEN_ON_ENTER)
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(AboutAppFragment::class.java)
            }

            clRateUs.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.open(Target.BrowserApp(BuildConfig.APP_URL))
            }
            switchOpenTrip.setOnCheckedChangeListener { _, isChecked ->
                viewModel.saveOpenTripState(isChecked)
            }
        }

        viewModel.getOpenTripStateObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable) {
                binding?.switchOpenTrip?.isChecked = it
            }
        viewModel.fetchOpenTripState()
    }
}