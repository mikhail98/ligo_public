package com.ligo.tools.api

import android.graphics.Bitmap
import android.net.Uri

interface ICompressor {

    companion object {
        const val BITMAP_WIDTH_IMAGE = 720
        const val BITMAP_WIDTH_QR = 720
    }

    fun compressImageToByteArray(uri: Uri, size: Int = BITMAP_WIDTH_IMAGE): ByteArray

    fun compressImageToBitmap(uri: Uri, size: Int = BITMAP_WIDTH_QR): Bitmap
}