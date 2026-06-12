@file:OptIn(ExperimentalForeignApi::class)

package com.siteflow.signature.core.presentation.components.image

import com.siteflow.signature.core.domain.model.NormalizedRect
import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*

actual object ImageAnnotator {

    actual fun drawRectanglesAndSave(
        imagePath: String,
        rects: List<NormalizedRect>,
        color: Long, // kept for expect/actual match (not used)
        onDone: (String) -> Unit
    ) {
        val image = UIImage.imageWithContentsOfFile(imagePath) ?: return

        if (rects.isEmpty()) {
            onDone(imagePath)
            return
        }

        val size = image.size
        val width = size.useContents { width }
        val height = size.useContents { height }

        val format = UIGraphicsImageRendererFormat.defaultFormat().apply {
            scale = image.scale
            opaque = false
        }

        val renderer = UIGraphicsImageRenderer(
            size = CGSizeMake(width, height),
            format = format
        )

        val renderedImage = renderer.imageWithActions { context ->
            context ?: return@imageWithActions

            image.drawInRect(
                CGRectMake(0.0, 0.0, width, height)
            )

            val ctx = context.CGContext

            rects.forEach { r ->
                val a = ((color shr 24) and 0xFF).toDouble() / 255.0
                val red = ((color shr 16) and 0xFF).toDouble() / 255.0
                val green = ((color shr 8) and 0xFF).toDouble() / 255.0
                val blue = (color and 0xFF).toDouble() / 255.0

                CGContextSetStrokeColor(
                    ctx,
                    cValuesOf(red, green, blue, a)
                )

                CGContextSetLineWidth(
                    ctx,
                    5.0
                )

                CGContextStrokeRect(
                    ctx,
                    CGRectMake(
                        r.left * width,
                        r.top * height,
                        (r.right - r.left) * width,
                        (r.bottom - r.top) * height
                    )
                )
            }
        }

        val jpegData =
            UIImageJPEGRepresentation(renderedImage, 0.95) ?: return

        val outputPath =
            NSTemporaryDirectory() +
                    "recce_final_${NSDate().timeIntervalSince1970}.jpg"

        jpegData.writeToFile(outputPath, true)

        onDone(outputPath)
    }
}
