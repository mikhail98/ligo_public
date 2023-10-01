package com.ligo.feature.delivery

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator

class DeliveryFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
) : BaseViewModel(navigator, analytics)
