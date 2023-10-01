package com.ligo.subfeature.createparcel.parceldetails

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelDetails(
    var weight: Int = -1,
    var parcelPhotoUrl: String? = null,
    var typeList: List<String> = mutableListOf(),
) : Parcelable {

    val isFilled: Boolean
        get() = weight != -1 && typeList.isNotEmpty() && parcelPhotoUrl != null

    fun reset() {
        weight = -1
        typeList = mutableListOf()
        parcelPhotoUrl = null
    }
}