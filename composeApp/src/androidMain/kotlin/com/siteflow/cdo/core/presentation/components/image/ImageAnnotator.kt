package com.siteflow.cdo.core.presentation.components.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.siteflow.cdo.core.domain.model.NormalizedRect
import java.io.File
import java.io.FileOutputStream

actual object ImageAnnotator {

    actual fun drawRectanglesAndSave(
        imagePath: String,
        rects: List<NormalizedRect>,
        color: Long,
        onDone: (String) -> Unit
    ) {
        val bitmap = BitmapFactory.decodeFile(imagePath)
            .copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(bitmap)

        rects.forEach { r ->
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                this.color = color.toInt()
                strokeWidth = 5f
            }

            canvas.drawRect(
                r.left * bitmap.width,
                r.top * bitmap.height,
                r.right * bitmap.width,
                r.bottom * bitmap.height,
                paint
            )
        }

        val outFile = File(
            File(imagePath).parent,
            "recce_final_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(outFile).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }

        onDone(outFile.absolutePath)
    }
}
