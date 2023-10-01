package com.ligo.subfeature.ratesender

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator

class RateSenderBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
) :
    BaseViewModel(navigator, analytics)