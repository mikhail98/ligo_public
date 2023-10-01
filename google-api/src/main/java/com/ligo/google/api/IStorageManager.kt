package com.ligo.google.api

import android.net.Uri
import io.reactivex.rxjava3.core.Single

interface IStorageManager {

    fun uploadMedia(uri: Uri, type: FileType, userEmail: String): Single<String>

    enum class FileType {
        PHOTO, VIDEO, AUDIO
    }

    class UploadException(
        val uri: Uri,
        val type: FileType,
    ) : Throwable()
}