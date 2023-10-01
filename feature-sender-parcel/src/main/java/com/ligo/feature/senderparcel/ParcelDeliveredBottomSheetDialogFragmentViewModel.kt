package com.ligo.feature.senderparcel

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator

class ParcelDeliveredBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
) :
    BaseViewModel(navigator, analytics)