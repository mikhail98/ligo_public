package com.ligo.google.api

import com.ligo.core.R
import com.ligo.data.model.ConfigStringKey

data class RemoteConfigCurrencies(
    val currencies: List<RemoteConfigCurrency>,
)

open class RemoteConfigCurrency(
    val code: String,
    open val fullNameKey: String,
    val iconRes: Int,
) {

    data class Euro(
        override val fullNameKey: String = ConfigStringKey.CURRENCY_FULL_NAME_EUR,
    ) : RemoteConfigCurrency(code = "EUR", fullNameKey, iconRes = R.drawable.eur)

    data class Pln(
        override val fullNameKey: String = ConfigStringKey.CURRENCY_FULL_NAME_PLN,
    ) : RemoteConfigCurrency(code = "PLN", fullNameKey, iconRes = R.drawable.pln)

    data class Usd(
        override val fullNameKey: String = ConfigStringKey.CURRENCY_FULL_NAME_USD,
    ) : RemoteConfigCurrency(code = "USD", fullNameKey, R.drawable.usd)

    companion object {
        fun fromCode(code: String, defaultFullNameKey: String = ""): RemoteConfigCurrency {
            return when (code) {
                Euro().code -> Euro()
                Pln().code -> Pln()
                Usd().code -> Usd()
                else -> RemoteConfigCurrency(
                    code = code,
                    fullNameKey = defaultFullNameKey,
                    iconRes = R.drawable.ic_unknown_currency
                )
            }
        }
    }
}