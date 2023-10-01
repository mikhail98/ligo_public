package com.ligo.feature.parcelintrip

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator

class ConfirmPickupParcelBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
) :
    BaseViewModel(navigator, analytics)