package com.ligo.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.ligo.core.printError
import com.ligo.tools.api.ICompressor
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

internal class Compressor(private val context: Context) : ICompressor {

    companion object {
        private const val ROTATE_0 = 0
        private const val ROTATE_90 = 90
        private const val ROTATE_180 = 180
        private const val ROTATE_270 = 270

        private const val BITMAP_COMPRESS_LEVEL = 70
    }

    override fun compressImageToBitmap(uri: Uri, size: Int): Bitmap {
        val compressedByteArray = compressImageToByteArray(uri, size)
        return BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.size)
    }

    private fun InputStream?.getRotationAngle(): Int {
        val orientation = ExifInterface(this ?: return ROTATE_0)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> ROTATE_90
            ExifInterface.ORIENTATION_ROTATE_180 -> ROTATE_180
            ExifInterface.ORIENTATION_ROTATE_270 -> ROTATE_270
            else -> ROTATE_0
        }
    }

    override fun compressImageToByteArray(uri: Uri, size: Int): ByteArray {
        val defaultArray = ByteArray(0)
        var sourceBitmap: Bitmap? = null
        val matrix = Matrix()

        try {
            var imageStream = context.contentResolver.openInputStream(uri)
            sourceBitmap = BitmapFactory.decodeStream(BufferedInputStream(imageStream))
            imageStream?.close()

            imageStream = context.contentResolver.openInputStream(uri)
            matrix.postRotate(imageStream.getRotationAngle().toFloat())
            imageStream?.close()
        } catch (e: FileNotFoundException) {
            printError(e)
        }

        sourceBitmap = sourceBitmap ?: return defaultArray

        val rotatedBitmap = Bitmap.createBitmap(
            sourceBitmap,
            0,
            0,
            sourceBitmap.width,
            sourceBitmap.height,
            matrix,
            true
        )
        var newBitmap = rotatedBitmap
        val scale = newBitmap.width.toFloat() / size
        val newHeight = (newBitmap.height / scale).toInt()
        newBitmap = Bitmap.createScaledBitmap(rotatedBitmap, size, newHeight, false)

        val stream = ByteArrayOutputStream()
        newBitmap.compress(Bitmap.CompressFormat.JPEG, BITMAP_COMPRESS_LEVEL, stream)

        try {
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stream.toByteArray()
    }
}
