package com.ligo.feature.main

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.IQrManager

class MainActivityViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val qrManager: IQrManager,
    private val photoManager: IPhotoManager,
) : BaseViewModel(navigator, analytics) {

    fun setupActivity(activity: MainActivity) {
        navigator.setupActivity(activity)
        qrManager.setupActivity(activity)
        photoManager.setupActivity(activity)
    }
}