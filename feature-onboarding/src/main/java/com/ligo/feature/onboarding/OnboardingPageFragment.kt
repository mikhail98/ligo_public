package com.ligo.feature.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ligo.common.ViewContainer
import com.ligo.common.withBindingSafety
import com.ligo.feature.onboarding.databinding.FragmentOnboardingPageBinding
import com.ligo.tools.api.ILocalizationManager
import org.koin.android.ext.android.inject

internal class OnboardingPageFragment : Fragment(), ViewContainer {

    companion object {

        private const val PAGE_ARGS_KEY = "onboarding_page_key"

        fun newInstance(page: OnboardingPage): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = bundleOf(PAGE_ARGS_KEY to page)
            }
        }
    }

    override val localizationManager: ILocalizationManager by inject()

    private var binding: FragmentOnboardingPageBinding? = null

    private val page: OnboardingPage? by lazy { arguments?.getParcelable(PAGE_ARGS_KEY) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val page = page ?: return
        withBindingSafety(binding) {
            ivMainImage.setImageResource(page.mainImageRes)
            tvTitle.text = getLocalizedStringByKey(page.titleConfigStringKey)
            tvDescription.text = getLocalizedStringByKey(page.descriptionConfigStringKey)
        }
    }
}