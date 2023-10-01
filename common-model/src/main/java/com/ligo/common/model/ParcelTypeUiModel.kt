package com.ligo.common.model

import androidx.annotation.DrawableRes
import com.ligo.core.R
import com.ligo.google.api.RemoteConfigParcelType

data class ParcelTypeUiModel(
    val type: String,
    var isChecked: Boolean,
    val titleKey: String,
    val descriptionKey: String,
    @DrawableRes val iconResId: Int,
) {
    companion object {
        fun fromRemoteConfigParcelType(
            remoteParcelType: RemoteConfigParcelType,
            selectedParcelTypeList: List<String> = listOf(),
        ): ParcelTypeUiModel {
            val titleKey = remoteParcelType.titleKey
            val descriptionKey = remoteParcelType.descriptionKey

            return ParcelTypeUiModel(
                type = remoteParcelType.type,
                isChecked = selectedParcelTypeList.contains(remoteParcelType.type),
                titleKey = titleKey,
                descriptionKey = descriptionKey,
                iconResId = R.drawable.ic_parcel
            )
        }
    }
}