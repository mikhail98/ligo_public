package com.ligo.navigator.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PushArgs(
    val homeTarget: HomeTarget = HomeTarget.DELIVERY,
    val availableParcelId: String? = null,
    val chatId: String? = null,
) : Parcelable {

    enum class HomeTarget {
        DELIVERY, HISTORY, PROFILE
    }
}