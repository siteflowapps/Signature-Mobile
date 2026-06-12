package com.siteflow.signature.core.domain

import com.siteflow.signature.core.util.toByteArray
import com.siteflow.signature.core.util.toNSData
import com.siteflow.signature.outlet.invoices.domain.InvoiceFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextScaleCTM
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSMutableData
import platform.PDFKit.PDFDocument
import platform.PDFKit.kPDFDisplayBoxMediaBox
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPageWithInfo
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

/**
 * iOS actual for [PdfCompressor].
 *
 * Pipeline (runs on [Dispatchers.Default]):
 *  1. Size / page-count gates (fail fast).
 *  2. [PDFDocument] from bytes — `null` ⇒ corrupt; `isEncrypted && isLocked` ⇒ encrypted.
 *  3. Scan copy = the untouched original bytes (passthrough). The backend rasterizes
 *     PDFs to 200-DPI PNG itself, so the pristine original gives GPT-4o the best render.
 *  4. Storage copy = walk [PdfCompressorConstants.ATTEMPTS] and rasterize to the first
 *     attempt within `targetBytesFor(pageCount)` (or the smallest attempt if none fit),
 *     purely to save server disk. The upload never fails on compression.
 */
@OptIn(ExperimentalForeignApi::class)
actual class PdfCompressor {

    actual suspend fun compress(bytes: ByteArray): PdfCompressionResult =
        withContext(Dispatchers.Default) {
            val originalKb = bytes.size / 1024

            if (bytes.size > PdfCompressorConstants.MAX_INPUT_BYTES) {
                log("Rejected — input ${originalKb}KB > ${PdfCompressorConstants.MAX_INPUT_BYTES / 1024}KB")
                return@withContext PdfCompressionResult.Failure.FileTooLarge
            }

            val nsData = bytes.toNSData()
            val document = PDFDocument(data = nsData)
                ?: return@withContext PdfCompressionResult.Failure.Corrupt

            if (document.isEncrypted() && document.isLocked()) {
                return@withContext PdfCompressionResult.Failure.Encrypted
            }

            val pageCount = document.pageCount.toInt()
            if (pageCount <= 0) return@withContext PdfCompressionResult.Failure.Corrupt
            if (pageCount > PdfCompressorConstants.MAX_PAGES) {
                log("Rejected — $pageCount pages > ${PdfCompressorConstants.MAX_PAGES}")
                return@withContext PdfCompressionResult.Failure.TooManyPages(pageCount)
            }

            val thumbnail = renderFirstPageThumbnail(document)
            val budget = PdfCompressorConstants.targetBytesFor(pageCount)
            log("Storage budget for $pageCount-page PDF: ${budget / 1024}KB")

            // Storage copy: walk the ladder and take the first attempt within budget;
            // if none fit, keep the smallest attempt. Never reject — the scan copy is
            // the untouched original, so the upload always succeeds.
            var bestStorage: ByteArray? = null
            for (attempt in PdfCompressorConstants.ATTEMPTS) {
                val rebuilt = rebuildPdf(document, dpi = attempt.dpi, jpegQuality = attempt.quality)
                log("Attempt dpi=${attempt.dpi} q=${attempt.quality} → ${rebuilt.size / 1024}KB")
                if (bestStorage == null || rebuilt.size < bestStorage!!.size) bestStorage = rebuilt
                if (rebuilt.size <= budget) break
            }

            // If compression couldn't beat the original (already-lean digital PDFs),
            // just store the original too.
            val storageBytes = bestStorage
                ?.takeIf { it.size < bytes.size }
                ?: bytes
            log("Scan copy: original ${originalKb}KB (passthrough) | storage copy: ${storageBytes.size / 1024}KB")

            PdfCompressionResult.Success(
                scanFile = InvoiceFile.pdf(bytes = bytes, pageCount = pageCount),
                storageFile = InvoiceFile.pdf(bytes = storageBytes, pageCount = pageCount),
                originalKb = originalKb,
                finalKb = storageBytes.size / 1024,
                pageCount = pageCount,
                firstPageThumbnail = thumbnail,
            )
        }

    private fun renderFirstPageThumbnail(document: PDFDocument): ByteArray {
        val page = document.pageAtIndex(0u) ?: return ByteArray(0)
        val bounds = page.boundsForBox(kPDFDisplayBoxMediaBox)
        val (ptW, ptH) = bounds.useContents { size.width to size.height }
        if (ptW <= 0.0 || ptH <= 0.0) return ByteArray(0)

        // Target ~200pt wide thumbnail.
        val scale = 200.0 / ptW
        val pxW = ptW * scale
        val pxH = ptH * scale

        UIGraphicsBeginImageContextWithOptions(
            size = CGSizeMake(pxW, pxH),
            opaque = true,
            scale = 1.0,
        )
        val ctx = UIGraphicsGetCurrentContext()
        try {
            // White background.
            CGContextSetFillColorWithColor(ctx, UIColor.whiteColor.CGColor)
            CGContextFillRect(ctx, CGRectMake(0.0, 0.0, pxW, pxH))
            // Flip PDF coordinate system (origin bottom-left) into UIKit (top-left).
            CGContextTranslateCTM(ctx, 0.0, pxH)
            CGContextScaleCTM(ctx, scale, -scale)
            page.drawWithBox(kPDFDisplayBoxMediaBox, toContext = ctx)
            val rendered = UIGraphicsGetImageFromCurrentImageContext() ?: return ByteArray(0)
            val jpeg = UIImageJPEGRepresentation(rendered, 0.7) ?: return ByteArray(0)
            return jpeg.toByteArray()
        } finally {
            UIGraphicsEndImageContext()
        }
    }

    private fun rebuildPdf(document: PDFDocument, dpi: Int, jpegQuality: Int): ByteArray {
        val outData = NSMutableData()
        UIGraphicsBeginPDFContextToData(outData, CGRectMake(0.0, 0.0, 0.0, 0.0), null)
        try {
            val total = document.pageCount.toInt()
            for (i in 0 until total) {
                val page = document.pageAtIndex(i.toULong()) ?: continue
                val bounds = page.boundsForBox(kPDFDisplayBoxMediaBox)
                val (ptW, ptH) = bounds.useContents { size.width to size.height }
                if (ptW <= 0.0 || ptH <= 0.0) continue

                // Rasterize at target DPI (PDF base = 72 DPI).
                val pxScale = dpi / 72.0
                val pxW = ptW * pxScale
                val pxH = ptH * pxScale

                val rendered = renderPageToImage(page = page, pxW = pxW, pxH = pxH, pxScale = pxScale)
                    ?: continue
                val jpeg = UIImageJPEGRepresentation(rendered, jpegQuality / 100.0) ?: continue
                val jpegImage = UIImage.imageWithData(jpeg) ?: continue

                val pageRect = CGRectMake(0.0, 0.0, ptW, ptH)
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                jpegImage.drawInRect(pageRect)
            }
            UIGraphicsEndPDFContext()
            return outData.toByteArray()
        } catch (t: Throwable) {
            runCatching { UIGraphicsEndPDFContext() }
            throw t
        }
    }

    private fun renderPageToImage(
        page: platform.PDFKit.PDFPage,
        pxW: Double,
        pxH: Double,
        pxScale: Double,
    ): UIImage? {
        UIGraphicsBeginImageContextWithOptions(
            size = CGSizeMake(pxW, pxH),
            opaque = true,
            scale = 1.0,
        )
        val ctx = UIGraphicsGetCurrentContext()
        try {
            CGContextSetFillColorWithColor(ctx, UIColor.whiteColor.CGColor)
            CGContextFillRect(ctx, CGRectMake(0.0, 0.0, pxW, pxH))
            CGContextTranslateCTM(ctx, 0.0, pxH)
            CGContextScaleCTM(ctx, pxScale, -pxScale)
            page.drawWithBox(kPDFDisplayBoxMediaBox, toContext = ctx)
            return UIGraphicsGetImageFromCurrentImageContext()
        } finally {
            UIGraphicsEndImageContext()
        }
    }

    private fun log(msg: String) = println("[PdfCompressor/iOS] $msg")
}

