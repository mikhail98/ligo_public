package com.ligo.feature.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.ligo.common.BaseFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.onboarding.databinding.FragmentOnboardingBinding
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.OnboardingType
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class OnboardingFragment : BaseFragment<OnboardingFragmentViewModel>() {

    companion object {

        const val TAG_BACKSTACK = "onboarding"
        private const val TYPE_ARGS_KEY = "type_args_key"

        fun newInstance(type: OnboardingType): Fragment {
            return OnboardingFragment().apply {
                arguments = bundleOf(TYPE_ARGS_KEY to type)
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            withBindingSafety(binding) {
                if (vpPages.currentItem != 0) {
                    vpPages.currentItem = vpPages.currentItem - 1
                }
            }
        }
    }

    override val koinModule: Module = OnboardingModule
    override val viewModel by inject<OnboardingFragmentViewModel>()

    override var registerBackpressureCallback: Boolean = false

    private var binding: FragmentOnboardingBinding? = null

    private val pages: List<OnboardingPage> by lazy {
        arguments?.getParcelable<OnboardingType>(TYPE_ARGS_KEY)?.let { type ->
            OnboardingPagesFactory.createPages(type)
        } ?: emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvSkip.setLocalizedTextByKey(ConfigStringKey.SKIP)
            btnNext.setLocalizedTextByKey(ConfigStringKey.NEXT)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        withBindingSafety(binding) {
            vpPages.offscreenPageLimit = 3

            btnNext.clipToOutline = true

            registerAnalytics()
            vpPages.adapter = OnboardingAdapter(this@OnboardingFragment, pages)
            dotsIndicator.attachTo(vpPages)
        }
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            tvSkip.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(OnboardingFragment::class.java)
            }

            btnNext.setOnThrottleClickListener(startedCompositeDisposable) {
                if (vpPages.currentItem == pages.size - 1) {
                    navigator.closeFragment(OnboardingFragment::class.java)
                } else {
                    vpPages.currentItem = vpPages.currentItem + 1
                }
            }
        }
    }

    private fun registerAnalytics() {
        binding?.vpPages?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.logEvent(
                    IAnalytics.Events.ACTION_SCREEN_OPENED + "_${position + 1}"
                )
            }
        })
    }
}