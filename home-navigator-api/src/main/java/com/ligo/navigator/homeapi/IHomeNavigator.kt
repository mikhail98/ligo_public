package com.ligo.navigator.homeapi

import androidx.fragment.app.Fragment

interface IHomeNavigator {

    fun setup(fragment: Fragment, homeContainerId: Int)

    fun open(target: HomeTarget)
}