package com.ligo.google

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.ligo.core.printError
import com.ligo.google.api.IStorageManager
import com.ligo.tools.api.ICompressor
import io.reactivex.rxjava3.core.Single

internal class StorageManager(
    private val compressor: ICompressor,
    private val firebaseStorage: FirebaseStorage,
) : IStorageManager {

    companion object {
        private const val FOLDER_PHOTO = "photos"
        private const val FOLDER_VIDEO = "videos"
        private const val FOLDER_AUDIO = "audios"

        private const val URL_SLASH = "%2F"

        private const val JPEG_EXT = ".jpeg"
        private const val MP4_EXT = ".mp4"

        private const val STORAGE_BUCKET = "pingo-demo.appspot.com"
        private const val FIREBASE_URL =
            "https://firebasestorage.googleapis.com/v0/b/$STORAGE_BUCKET/o/"
        private const val ALT_MEDIA = "?alt=media"
    }

    private fun getFolderForFile(userEmail: String, type: IStorageManager.FileType): String {
        val mediaFolder = when (type) {
            IStorageManager.FileType.PHOTO -> FOLDER_PHOTO
            IStorageManager.FileType.VIDEO -> FOLDER_VIDEO
            IStorageManager.FileType.AUDIO -> FOLDER_AUDIO
        }
        val mediaExtension = when (type) {
            IStorageManager.FileType.PHOTO -> JPEG_EXT
            IStorageManager.FileType.VIDEO -> MP4_EXT
            IStorageManager.FileType.AUDIO -> MP4_EXT
        }
        return userEmail
            .plus("/")
            .plus(mediaFolder)
            .plus("/")
            .plus(System.currentTimeMillis())
            .plus(mediaExtension)
    }

    override fun uploadMedia(
        uri: Uri,
        type: IStorageManager.FileType,
        userEmail: String,
    ): Single<String> {
        return Single.create { emitter ->
            firebaseStorage.reference.child(getFolderForFile(userEmail, type))
                .putBytes(compressor.compressImageToByteArray(uri))
                .addOnSuccessListener { taskSnapshot ->
                    val urlPart = taskSnapshot.metadata?.path?.replace("/", URL_SLASH)
                    if (urlPart != null) {
                        val url = FIREBASE_URL.plus(urlPart).plus(ALT_MEDIA)
                        emitter.onSuccess(url)
                    } else {
                        emitter.onError(IStorageManager.UploadException(uri, type))
                    }
                }
                .addOnFailureListener {
                    printError(it)
                    emitter.onError(IStorageManager.UploadException(uri, type))
                }
        }
    }
}