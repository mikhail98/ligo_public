package com.ligo.feature.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class OnboardingAdapter(
    fragment: Fragment,
    private val items: List<OnboardingPage>,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = items.size

    override fun createFragment(position: Int): Fragment {
        return OnboardingPageFragment.newInstance(items[position])
    }
}