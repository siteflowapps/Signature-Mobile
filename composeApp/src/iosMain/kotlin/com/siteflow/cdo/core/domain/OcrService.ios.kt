package com.siteflow.cdo.core.domain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.UIKit.UIImage
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.coroutines.resume

actual class OcrService {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun extractText(imagePath: String): OcrResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val data = NSData.dataWithContentsOfFile(imagePath)
                if (data == null) {
                    continuation.resume(OcrResult.failure("Image file not found"))
                    return@suspendCancellableCoroutine
                }

                val image = UIImage(data = data)
                val cgImage = image.CGImage
                if (cgImage == null) {
                    continuation.resume(OcrResult.failure("Could not create CGImage"))
                    return@suspendCancellableCoroutine
                }

                val lines = mutableListOf<String>()
                val rawTextBuilder = StringBuilder()

                val request = VNRecognizeTextRequest { request, error ->
                    if (error != null) {
                        continuation.resume(
                            OcrResult.failure("OCR failed: ${error.localizedDescription}")
                        )
                        return@VNRecognizeTextRequest
                    }

                    val observations = request?.results
                    if (observations != null) {
                        for (obs in observations) {
                            val textObs = obs as? VNRecognizedTextObservation ?: continue
                            val candidates = textObs.topCandidates(1u)
                            for (candidate in candidates) {
                                val text = candidate.toString()
                                if (text.isNotBlank()) {
                                    lines.add(text)
                                    rawTextBuilder.appendLine(text)
                                }
                            }
                        }
                    }

                    continuation.resume(
                        OcrResult(
                            rawText = rawTextBuilder.toString().trim(),
                            lines = lines
                        )
                    )
                }

                request.setRecognitionLevel(VNRequestTextRecognitionLevelAccurate)

                val handler = VNImageRequestHandler(cgImage, options = emptyMap<Any?, Any>())
                val nsError: kotlinx.cinterop.ObjCObjectVar<platform.Foundation.NSError?>? = null
                handler.performRequests(listOf(request), error = null)

            } catch (e: Exception) {
                continuation.resume(
                    OcrResult.failure("OCR error: ${e.message}")
                )
            }
        }
    }
}
