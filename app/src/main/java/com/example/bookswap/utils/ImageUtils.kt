package com.example.bookswap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 500): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            var quality = 90
            var compressedData: ByteArray

            do {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedData = outputStream.toByteArray()
                quality -= 10
            } while (compressedData.size > maxSizeKB * 1024 && quality > 10)

            compressedData
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}