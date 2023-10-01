package com.ligo.subfeature.createtrip

import com.ligo.common.BaseViewModel
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator

class DatePickerBottomSheetDialogFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
) :
    BaseViewModel(navigator, analytics)