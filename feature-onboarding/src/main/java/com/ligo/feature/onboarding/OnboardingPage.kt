package com.ligo.feature.onboarding

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnboardingPage(
    val titleConfigStringKey: String,
    val descriptionConfigStringKey: String,
    @DrawableRes val mainImageRes: Int,
) : Parcelable