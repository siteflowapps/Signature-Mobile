package com.siteflow.cdo.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File

actual object ImageEncoder {
    actual fun encodeToBytes(imagePath: String, maxSize: Long): ByteArray? {
        return try {
            val file = File(imagePath)
            if (!file.exists()) return null
            
            // If already small enough, return raw bytes
            if (file.length() <= maxSize) {
                return file.readBytes()
            }

            // Decode bitmap
            var bitmap = BitmapFactory.decodeFile(imagePath) ?: return null
            
            // Resize if large (e.g., > 2048px)
            val maxDim = 2048
            if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val newWidth = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
                val newHeight = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            }

            // Compress loop
            var quality = 90
            var stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            while (stream.size() > maxSize && quality > 10) {
                stream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
