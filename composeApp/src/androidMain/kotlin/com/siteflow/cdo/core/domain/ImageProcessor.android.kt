package com.siteflow.cdo.core.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.ExifInterface
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual class ImageProcessor(private val context: Context) {

    actual suspend fun processImage(
        originalPath: String,
        latitude: Double?,
        longitude: Double?
    ): String = withContext(Dispatchers.IO) {
        try {
            processImageInternal(originalPath, latitude, longitude)
        } catch (e: Exception) {
            // If any error occurs, log it and return the original path so the photo still appears
            Log.e("ImageProcessor", "Failed to process image: ${e.message}", e)
            originalPath
        }
    }

    private fun processImageInternal(
        originalPath: String,
        latitude: Double?,
        longitude: Double?
    ): String {
        Log.d("ImageProcessor", "processImage called: path=$originalPath lat=$latitude lng=$longitude")

        val originalFile = File(originalPath)
        if (!originalFile.exists()) {
            Log.e("ImageProcessor", "File does not exist: $originalPath")
            return originalPath
        }
        Log.d("ImageProcessor", "File exists, size=${originalFile.length()} bytes")

        // 1. Decode bounds first
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(originalPath, boundsOptions)
        Log.d("ImageProcessor", "Image dims: ${boundsOptions.outWidth}x${boundsOptions.outHeight}")
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            Log.e("ImageProcessor", "Failed to decode image bounds")
            return originalPath
        }

        // Compute scale: target max dimension of 1280px
        val maxDimension = 1280
        var scale = 1
        val longerSide = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
        while (longerSide / (scale * 2) >= maxDimension) { scale *= 2 }
        Log.d("ImageProcessor", "Using inSampleSize=$scale")

        // 2. Load scaled bitmap
        val decodeOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = scale
        }
        val rawBitmap = BitmapFactory.decodeFile(originalPath, decodeOptions)
        if (rawBitmap == null) {
            Log.e("ImageProcessor", "BitmapFactory.decodeFile returned null!")
            return originalPath
        }
        Log.d("ImageProcessor", "Decoded bitmap: ${rawBitmap.width}x${rawBitmap.height}")

        // 3. Fix EXIF rotation
        val bitmap = try {
            val exif = ExifInterface(originalPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            Log.d("ImageProcessor", "EXIF orientation: $orientation")
            rotateBitmap(rawBitmap, orientation)
        } catch (e: Exception) {
            Log.w("ImageProcessor", "EXIF read failed: ${e.message}")
            rawBitmap
        }

        // 4. Burn watermark into bitmap
        Log.d("ImageProcessor", "Adding watermark...")
        val watermarked = addWatermark(bitmap, latitude, longitude)
        if (bitmap !== watermarked) bitmap.recycle()
        Log.d("ImageProcessor", "Watermark applied. Bitmap: ${watermarked.width}x${watermarked.height}")

        // 5. Compress as JPEG — solid background means text survives compression fine
        val outFile = File(context.cacheDir, "processed_${System.currentTimeMillis()}.jpg")
        var quality = 85
        var bytes = ByteArray(0)
        var sizeKb: Int
        do {
            val baos = ByteArrayOutputStream()
            watermarked.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
            sizeKb = bytes.size / 1024
            Log.d("ImageProcessor", "Compressed at q=$quality -> ${sizeKb}KB")
            quality -= 10
        } while (sizeKb > 500 && quality >= 20)
        FileOutputStream(outFile).use { it.write(bytes) }
        val written = true
        Log.d("ImageProcessor", "Saved to: ${outFile.absolutePath} (${sizeKb}KB)")
        watermarked.recycle()

        if (!written) {
            Log.e("ImageProcessor", "Failed to write output file!")
            return originalPath
        }

        // 6. Remove original temp file
        if (originalPath.startsWith(context.cacheDir.absolutePath)) {
            try { originalFile.delete() } catch (_: Exception) {}
        }

        Log.d("ImageProcessor", "Done! Returning: ${outFile.absolutePath}")
        return outFile.absolutePath
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.postRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotated
    }

    @Suppress("DEPRECATION")
    private fun addWatermark(original: Bitmap, lat: Double?, lon: Double?): Bitmap {
        val result = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val w = result.width
        val h = result.height

        val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val line1 = "DATE: ${dateFmt.format(Date())}"
        val line2 = if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {
            "GPS: ${"%.5f".format(lat)}, ${"%.5f".format(lon)}"
        } else {
            "GPS: N/A"
        }
        val watermarkText = "$line1\n$line2"

        Log.d("ImageProcessor", "Watermark text: $watermarkText")

        // TextPaint for StaticLayout - LARGE text, no anti-alias for crisp pixels
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (w * 0.065f).coerceAtLeast(50f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Build StaticLayout (handles text measurement and rendering properly)
        val textWidth = (w * 0.94f).toInt() // 94% of image width for text
        @Suppress("DEPRECATION")
        val layout = StaticLayout(
            watermarkText,
            textPaint,
            textWidth,
            android.text.Layout.Alignment.ALIGN_NORMAL,
            1.3f,  // line spacing multiplier
            0f,    // line spacing extra
            false
        )

        val textBlockHeight = layout.height
        val padV = (h * 0.015f).toInt()
        val bannerHeight = textBlockHeight + padV * 2

        Log.d("ImageProcessor", "StaticLayout: textH=$textBlockHeight bannerH=$bannerHeight w=$w h=$h")

        // 1. Draw solid dark green banner (debug: very obvious, swap to dark/black for prod)
        val bgPaint = Paint().apply {
            color = Color.argb(220, 0, 0, 0) // dark, semi-transparent black
            style = Paint.Style.FILL
            isAntiAlias = false
        }
        val bannerTop = h - bannerHeight
        canvas.drawRect(0f, bannerTop.toFloat(), w.toFloat(), h.toFloat(), bgPaint)

        // 2. Translate canvas to text start position and render via StaticLayout
        val padH = (w * 0.03f).toInt()
        canvas.save()
        canvas.translate(padH.toFloat(), (bannerTop + padV).toFloat())
        layout.draw(canvas)
        canvas.restore()

        Log.d("ImageProcessor", "Watermark drawn via StaticLayout at y=$bannerTop")

        return result
    }
}

