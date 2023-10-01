package com.ligo.subfeature.createparcel

import com.ligo.data.model.Location
import com.ligo.subfeature.createparcel.parceldetails.ParcelDetails

data class CreateParcelData(
    var weight: Int = -1,
    var reward: Int = -1,
    var startPoint: Location? = null,
    var endPoint: Location? = null,
    var types: List<String> = mutableListOf(),
    var currencyCode: String,
    var parcelPhotoUrl: String? = null,
) {

    val isFilled: Boolean
        get() = weight != -1 && reward != -1 && startPoint != null && endPoint != null && types.isNotEmpty() && parcelPhotoUrl != null

    val parcelDetails: ParcelDetails
        get() = ParcelDetails(weight, parcelPhotoUrl, types)

    fun reset(currencyCode: String) {
        weight = -1
        reward = -1
        startPoint = null
        endPoint = null
        types = mutableListOf()
        parcelPhotoUrl = null
        this.currencyCode = currencyCode
    }
}