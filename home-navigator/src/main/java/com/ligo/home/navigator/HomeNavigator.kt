package com.ligo.home.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.ligo.feature.delivery.DeliveryFragment
import com.ligo.feature.history.HistoryFragment
import com.ligo.feature.profile.ProfileFragment
import com.ligo.navigator.homeapi.HomeTarget
import com.ligo.navigator.homeapi.IHomeNavigator

class HomeNavigator : IHomeNavigator {

    private var homeContainerId: Int? = null
    private var homeFragment: Fragment? = null

    override fun setup(fragment: Fragment, homeContainerId: Int) {
        this.homeFragment = fragment
        this.homeContainerId = homeContainerId
    }

    override fun open(target: HomeTarget) {
        val homeContainerId = homeContainerId ?: return
        val fragmentManager = homeFragment?.childFragmentManager ?: return
        val currentFragment = fragmentManager.findFragmentById(homeContainerId)

        val fragment: Fragment
        val fragmentExist: Boolean
        val tag: String
        when (target) {
            HomeTarget.Delivery -> {
                tag = DeliveryFragment.TAG
                fragment = findFragment(tag).also { fragmentExist = it != null }
                    ?: DeliveryFragment.newInstance()
            }

            HomeTarget.History -> {
                tag = HistoryFragment.TAG
                fragment = findFragment(tag).also { fragmentExist = it != null }
                    ?: HistoryFragment.newInstance()
            }

            HomeTarget.Profile -> {
                tag = ProfileFragment.TAG
                fragment = findFragment(tag).also { fragmentExist = it != null }
                    ?: ProfileFragment.newInstance()
            }
        }
        fragmentManager.commit {
            setCustomAnimations(
                android.R.animator.fade_in,
                android.R.animator.fade_out
            )
            if (currentFragment != null) {
                detach(currentFragment)
            }
            if (fragmentExist) {
                attach(fragment)
            } else {
                add(homeContainerId, fragment, tag)
            }
        }
    }

    private fun findFragment(tag: String): Fragment? =
        homeFragment?.childFragmentManager?.findFragmentByTag(tag)
}