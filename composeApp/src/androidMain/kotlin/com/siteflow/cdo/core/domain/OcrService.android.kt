package com.siteflow.cdo.core.domain

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

actual class OcrService(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    actual suspend fun extractText(imagePath: String): OcrResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val file = File(imagePath)
                if (!file.exists()) {
                    continuation.resume(OcrResult.failure("Image file not found"))
                    return@suspendCancellableCoroutine
                }

                val image = InputImage.fromFilePath(context, Uri.fromFile(file))

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val lines = mutableListOf<String>()
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                lines.add(line.text)
                            }
                        }
                        continuation.resume(
                            OcrResult(
                                rawText = visionText.text,
                                lines = lines
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(
                            OcrResult.failure("OCR failed: ${e.message}")
                        )
                    }
            } catch (e: Exception) {
                continuation.resume(
                    OcrResult.failure("OCR error: ${e.message}")
                )
            }
        }
    }
}
