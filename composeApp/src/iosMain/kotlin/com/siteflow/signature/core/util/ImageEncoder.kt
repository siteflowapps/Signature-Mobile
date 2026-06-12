package com.siteflow.signature.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual object ImageEncoder {
    actual fun encodeToBytes(imagePath: String, maxSize: Long): ByteArray? {
        return try {
            val fileManager = NSFileManager.defaultManager
            val data = fileManager.contentsAtPath(imagePath) ?: return null
            
            if (data.length.toLong() <= maxSize) {
                return data.toByteArray()
            }
            
            // Compress
            val image = UIImage.imageWithData(data) ?: return null
            var compression = 0.9
            var compressedData = UIImageJPEGRepresentation(image, compression)
            
            while ((compressedData?.length?.toLong() ?: 0L) > maxSize && compression > 0.1) {
                compression -= 0.1
                compressedData = UIImageJPEGRepresentation(image, compression)
            }
            
            compressedData?.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun NSData.toByteArray(): ByteArray {
        return ByteArray(length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), bytes, length)
            }
        }
    }
}
