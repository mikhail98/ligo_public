package com.ligo.navigator.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class OnboardingType(val typeKey: String) : Parcelable {
    MAIN("main")
}