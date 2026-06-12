package com.siteflow.signature.core.domain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSAttributedString
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIBezierPath
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.drawInRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

actual class ImageProcessor {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun processImage(
        originalPath: String,
        latitude: Double?,
        longitude: Double?
    ): String = withContext(Dispatchers.IO) {
        try {
            processImageInternal(originalPath, latitude, longitude)
        } catch (e: Exception) {
            println("ImageProcessor: Failed to process image: ${e.message}")
            originalPath
        }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    private fun processImageInternal(
        originalPath: String,
        latitude: Double?,
        longitude: Double?
    ): String {
        val originalImage = UIImage.imageWithContentsOfFile(originalPath) ?: return originalPath

        // 1. Scale to max 1280px on the longer side
        val maxDimension = 1280.0
        val originalWidth = originalImage.size.useContents { width }
        val originalHeight = originalImage.size.useContents { height }
        var targetWidth = originalWidth
        var targetHeight = originalHeight

        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            val ratio = if (originalWidth > originalHeight) maxDimension / originalWidth else maxDimension / originalHeight
            targetWidth = originalWidth * ratio
            targetHeight = originalHeight * ratio
        }

        val targetSize = CGSizeMake(targetWidth, targetHeight)

        // 2. Begin drawing context
        UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
        originalImage.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))

        // 3. Build watermark strings
        val dateFormatter = NSDateFormatter().apply { dateFormat = "dd/MM/yyyy  HH:mm:ss" }
        val line1 = "📅 ${dateFormatter.stringFromDate(NSDate())}"
        val line2 = if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
            val latStr = (latitude * 1_000_000).toLong().toDouble() / 1_000_000
            val lonStr = (longitude * 1_000_000).toLong().toDouble() / 1_000_000
            "📍 $latStr, $lonStr"
        } else {
            "📍 Location unavailable"
        }

        // 4. Compute banner dimensions
        val fontSize = targetWidth * 0.038
        val font = UIFont.boldSystemFontOfSize(fontSize)
        val paddingX = targetWidth * 0.025
        val paddingY = targetHeight * 0.018
        val lineGap = fontSize * 0.55
        val lineHeight = fontSize * 1.4
        val bannerHeight = (lineHeight * 2) + lineGap + (paddingY * 2.5)
        val bannerTop = targetHeight - bannerHeight

        // 5. Draw 80% opaque black background banner
        UIColor.colorWithWhite(0.0, 0.8).setFill()
        UIBezierPath.bezierPathWithRect(CGRectMake(0.0, bannerTop, targetWidth, bannerHeight)).fill()

        // 6. Draw a pink accent stripe at the top of the banner
        UIColor.colorWithRed(1.0, green = 0.25, blue = 0.51, alpha = 1.0).setFill()
        UIBezierPath.bezierPathWithRect(CGRectMake(0.0, bannerTop, targetWidth, fontSize * 0.12)).fill()

        // 7. Draw text using NSAttributedString.drawInRect (reliable in offscreen context)
        val textAttributes: Map<Any?, Any?> = mapOf(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to UIColor.whiteColor
        )

        val row1Top = bannerTop + paddingY
        val attrLine1 = NSAttributedString.create(string = line1, attributes = textAttributes)
        attrLine1.drawInRect(CGRectMake(paddingX, row1Top, targetWidth - (paddingX * 2), lineHeight))

        val row2Top = row1Top + lineHeight + lineGap
        val attrLine2 = NSAttributedString.create(string = line2, attributes = textAttributes)
        attrLine2.drawInRect(CGRectMake(paddingX, row2Top, targetWidth - (paddingX * 2), lineHeight))

        // 8. Get watermarked image
        val watermarkedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        if (watermarkedImage == null) return originalPath

        // 9. Compress to <= 400KB
        var quality = 0.88
        var finalData = UIImageJPEGRepresentation(watermarkedImage, quality)
        while (quality > 0.2 && finalData != null && finalData.length > 400_000u) {
            quality -= 0.1
            finalData = UIImageJPEGRepresentation(watermarkedImage, quality)
        }

        val fileName = "processed_${NSDate().timeIntervalSince1970}.jpg"
        val newPath = NSTemporaryDirectory() + fileName

        return if (finalData != null) {
            finalData.writeToFile(newPath, true)
            newPath
        } else {
            originalPath
        }
    }
}

