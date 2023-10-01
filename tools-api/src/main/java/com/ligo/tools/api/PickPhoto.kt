package com.ligo.tools.api

import android.net.Uri

data class PickPhoto(
    val uri: Uri,
    val source: Source,
    val origin: Origin,
    val realPath: String?,
) {
    enum class Source {
        GALLERY, CAMERA
    }

    enum class Origin {
        AVATAR, PARCEL_REJECTION, CHAT_PHOTO, PARCEL
    }

    enum class Type {
        REGULAR_PHOTO, PHOTO_PASSPORT, AVATAR, SCAN_QR
    }
}
