package com.ligo.subfeature.createparcel.currency

import com.ligo.google.api.RemoteConfigCurrency

data class CurrencyItemUi(
    val currency: RemoteConfigCurrency,
    var isSelected: Boolean = false,
)