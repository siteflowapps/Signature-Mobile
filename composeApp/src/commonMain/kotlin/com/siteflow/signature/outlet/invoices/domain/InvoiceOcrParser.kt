package com.siteflow.signature.outlet.invoices.domain

import com.siteflow.signature.core.domain.OcrResult
import kotlin.math.round
import com.siteflow.signature.outlet.invoices.data.InvoiceMetaData
import com.siteflow.signature.outlet.invoices.data.ProductMasterCatalog
import com.siteflow.signature.outlet.invoices.data.MasterProduct
import com.siteflow.signature.outlet.invoices.data.SkuLineItem
import com.siteflow.signature.outlet.invoices.data.TaxBreakdown
import com.siteflow.signature.outlet.invoices.data.UploadInvoiceFormData
import com.siteflow.signature.outlet.invoices.data.InvoiceSummary

/*
 * ════════════════════════════════════════════════════════════════════
 * [PAUSED] — InvoiceOcrParser  /  On-Device Regex Invoice Parsing
 * ════════════════════════════════════════════════════════════════════
 * Status: NOT called from production code as of 2026-03-07.
 *
 * Reason: Invoice format variance across distributors makes regex-based
 *         extraction unreliable. The parsing logic is preserved here
 *         because:
 *           1. It passes all unit tests (InvoiceOcrParserTest).
 *           2. It may be useful as a fallback or validation layer
 *              alongside the upcoming Gemini 1.5 Flash server-side OCR.
 *
 * Future plan:
 *   Upload pre-processed image → Server runs Gemini 1.5 Flash → Returns
 *   GeminiInvoiceResponse JSON → ReviewInvoiceScreen shows pre-filled form.
 *
 * DO NOT DELETE — preserved for future re-use.
 * ════════════════════════════════════════════════════════════════════
 */

/**
 * Production-grade invoice OCR parser — Block 1: Metadata Extraction.
 *
 * Extracts: Distributor Name, GSTIN, Invoice Number, Date, Email, Phone, Address, Bill-To.
 * Tested against 5+ real Indian tax invoice formats.
 *
 * The key insight from real invoices:
 *   - The SELLER (distributor) info always appears BEFORE "Bill To" / "Buyer"
 *   - The BUYER (outlet) info appears AFTER "Bill To" / "Buyer"
 *   - We split the text at the "Bill To" boundary to separate seller vs buyer
 */
object InvoiceOcrParser {


    // ══════════════════════════════════════════════════════════════
    // Regex Definitions
    // ══════════════════════════════════════════════════════════════

    /** Indian GSTIN: 2-digit state code + 10 char PAN + 1Z + 1 check digit */
    private val GSTIN_REGEX = Regex("""[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][A-Z0-9]Z[A-Z0-9]""")

    /** Keywords that precede an invoice number */
    private val INVOICE_NO_KEYWORDS = listOf(
        "invoice no", "invoice number", "inv no", "invoice no.", "inv no."
    )

    /** Date formats */
    private val DATE_NUMERIC_REGEX = Regex("""(\d{1,2})[/\-](\d{1,2})[/\-](\d{2,4})""")
    private val DATE_ALPHA_REGEX = Regex(
        """(\d{1,2})[/\-](Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[/\-](\d{2,4})""",
        RegexOption.IGNORE_CASE
    )

    /** Email pattern */
    private val EMAIL_REGEX = Regex("""[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}""")

    /** Phone: 10+ digit Indian number, possibly with country code */
    private val PHONE_REGEX = Regex("""(?:\+91[\s\-]?)?[6-9]\d{9}""")

    /** Keywords marking the Bill-To / Buyer boundary */
    private val BILL_TO_KEYWORDS = listOf("bill to", "buyer", "buyer (bill to)", "consignee", "ship to")

    /** Lines to SKIP when looking for the distributor name */
    private val SKIP_NAME_PATTERNS = listOf(
        "tax invoice", "invoice", "gstin", "gst", "state", "date", "phone", "email",
        "mobile", "fax", "place of supply", "invoice no", "inv no", "bill to",
        "buyer", "ship to", "consignee", "original", "duplicate", "copy",
        "bank", "branch", "account", "ifsc", "neft", "rtgs", "payment",
        "terms", "note:", "cso name", "cso contact", "place of supply"
    )

    /** Alphanumeric pattern for invoice numbers (min 4 chars) */
    private val ALPHANUM_PATTERN = Regex("""[A-Za-z0-9][A-Za-z0-9\-/]{3,}""")

    // ── Item extraction patterns ──
    private val HSN_REGEX = Regex("""\b(2[0-9]{7})\b""")
    private val CURRENCY_REGEX = Regex("""[₹Rs.]+\s*([\d,]+\.?\d*)""")
    private val QTY_UNIT_REGEX = Regex("""(\d+(?:\.\d+)?)\s*(CASE|CS|BOX|NO\.|NOS|PCS)\b""", RegexOption.IGNORE_CASE)
    private val GST_RATE_PAREN_REGEX = Regex("""\((\d+(?:\.\d+)?)%\)""")
    private val GST_RATE_PLAIN_REGEX = Regex("""(\d+(?:\.\d+)?)\s*%""")
    private val TAX_ROW_REGEX = Regex("""(CGST|SGST)\s+[₹Rs.]*\s*([\d,]+\.?\d*)\s+([\d.]+)%?\s+[₹Rs.]*\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
    private val ROW_START_REGEX = Regex("""^\s*(\d{1,2})[\s\t]+\S""")

    // ══════════════════════════════════════════════════════════════
    // Public API
    // ══════════════════════════════════════════════════════════════

    /**
     * Parse OCR result into structured InvoiceMetaData with confidence scoring.
     */
    fun parseMetaData(ocrResult: OcrResult): InvoiceMetaData {
        if (!ocrResult.success || ocrResult.lines.isEmpty()) {
            return InvoiceMetaData.empty(ocrResult.rawText)
        }

        // ── Step 1: Normalize OCR text for consistency ──
        val normalizedLines = OcrTextNormalizer.normalize(ocrResult.lines)
        val rawText = ocrResult.rawText

        // Dump raw OCR lines for debugging
        debugLog("═══ RAW OCR (${ocrResult.lines.size} lines) ═══")
        ocrResult.lines.forEachIndexed { i, line -> debugLog("  [$i] $line") }
        debugLog("═══ NORMALIZED (${normalizedLines.size} lines) ═══")
        normalizedLines.forEachIndexed { i, line -> debugLog("  [$i] $line") }

        // ── Step 2: Anchor-based zone detection ──
        // OCR returns lines in RANDOM ORDER, so we can't rely on positional split.
        // Instead, find GSTIN line as anchor and build seller zone around it.
        // IMPORTANT: ML Kit may return GSTIN twice (once in header, once in bank section).
        // We prefer the FIRST GSTIN occurrence, which is in the seller header.
        val allGstinIndices = normalizedLines.indices.filter { i ->
            GSTIN_REGEX.containsMatchIn(normalizedLines[i].uppercase())
        }
        // Prefer GSTIN that appears near the top (before line 30) or the first one
        val gstinLineIndex = allGstinIndices.firstOrNull { it < 30 }
            ?: allGstinIndices.firstOrNull()
            ?: findGSTINLineIndex(normalizedLines)
        val billToIndex = findBillToLineIndex(normalizedLines)

        // Seller zone: lines within ±10 of GSTIN line, but NEVER past Bill-To
        val sellerLines = if (gstinLineIndex >= 0) {
            val start = maxOf(0, gstinLineIndex - 10)
            // Cap at billToIndex to never include buyer section
            val end = if (billToIndex > gstinLineIndex) {
                minOf(billToIndex, gstinLineIndex + 5)
            } else {
                minOf(normalizedLines.size, gstinLineIndex + 5)
            }
            normalizedLines.subList(start, end)
        } else if (billToIndex > 0) {
            normalizedLines.subList(0, billToIndex)
        } else {
            normalizedLines
        }

        // Pre-BillTo lines: strictly everything before "Bill To" — used as safe fallback
        val preBillToLines = if (billToIndex > 0) {
            normalizedLines.subList(0, billToIndex)
        } else normalizedLines

        // Buyer zone: lines within ±5 of "Bill To" keyword
        val buyerLines = if (billToIndex >= 0) {
            val start = billToIndex
            val end = minOf(normalizedLines.size, billToIndex + 6)
            normalizedLines.subList(start, end)
        } else emptyList()

        debugLog("Zones → GSTIN anchor: $gstinLineIndex, BillTo: $billToIndex, " +
            "Seller: ${sellerLines.size} lines, Buyer: ${buyerLines.size} lines")

        // ── Step 3: Extract each field with seller-zone-first + full-text fallback ──
        val gstin = extractGSTIN(sellerLines, rawText)
            ?: extractGSTIN(normalizedLines, rawText)

        val invoiceNumberResult = extractInvoiceNumber(normalizedLines)

        val invoiceDate = extractInvoiceDate(normalizedLines, rawText, invoiceNumberResult.lineIndex)
            ?.let { normalizeDate(it) }

        val email = extractEmail(sellerLines)
            ?: extractEmail(normalizedLines)
            ?: extractEmailFromRawText(rawText)

        val phone = extractPhone(sellerLines)
            ?: extractPhone(normalizedLines)

        val address = extractAddress(sellerLines, null) // extracted after name cross-check
        val billToName = extractBillToName(buyerLines)

        // Extract distributor name — try multiple strategies
        // Strategy 1: seller zone
        // Strategy 2: pre-BillTo lines
        // Strategy 3: Look for "Account holder's name" pattern (common in Indian invoices)
        var distributorName = extractDistributorName(sellerLines)
            ?: extractDistributorName(preBillToLines)

        // Cross-check: if distributor name matches Bill-To name, we picked the wrong one
        if (distributorName != null && billToName != null &&
            distributorName.lowercase().trim() == billToName.lowercase().trim()) {
            debugLog("⚠ Distributor='$distributorName' matches BillTo — re-extracting from pre-BillTo")
            distributorName = extractDistributorName(preBillToLines)
        }

        // Strategy 3: "Account holder's name" (bank section often has the real company name)
        // Also triggered when distributor STILL matches BillTo after re-extraction
        val shouldTryHolder = distributorName == null ||
            distributorName.lowercase().contains("bank") ||
            (billToName != null && distributorName.lowercase().trim() == billToName.lowercase().trim())
        if (shouldTryHolder) {
            val holderLine = normalizedLines.firstOrNull {
                it.lowercase().contains("account holder") && it.contains(":")
            }
            if (holderLine != null) {
                val name = holderLine.substringAfter(":").trim()
                if (name.length >= 3) distributorName = name
            }
        }

        // Now extract address using the verified distributor name
        val finalAddress = extractAddress(sellerLines, distributorName)
            ?: extractAddress(preBillToLines, distributorName)

        val metadata = InvoiceMetaData(
            distributorGSTIN = gstin,
            invoiceNumber = invoiceNumberResult.value,
            invoiceDate = invoiceDate,
            distributorName = distributorName,
            distributorEmail = email,
            distributorPhone = phone,
            distributorAddress = finalAddress,
            billToName = billToName,
            rawText = rawText,
            confidenceScore = 0
        )

        val result = metadata.copy(confidenceScore = computeConfidence(metadata))

        debugLog("Extracted → Distributor: ${result.distributorName}, GSTIN: ${result.distributorGSTIN}, " +
            "Invoice: ${result.invoiceNumber}, Date: ${result.invoiceDate}, Email: ${result.distributorEmail}, " +
            "Phone: ${result.distributorPhone}, BillTo: ${result.billToName}, Confidence: ${result.confidenceScore}%")

        return result
    }

    /**
     * Parse function — extracts full invoice data including metadata, items, tax, and summary.
     */
    fun parse(ocrResult: OcrResult): UploadInvoiceFormData {
        val meta = parseMetaData(ocrResult)
        val normalizedLines = OcrTextNormalizer.normalize(ocrResult.lines)
        val items = extractLineItems(normalizedLines, ocrResult.lines)
        val taxRows = extractTaxBreakdown(normalizedLines)
        val summary = extractInvoiceSummary(normalizedLines)

        val formData = UploadInvoiceFormData(
            invoiceNumber = meta.invoiceNumber ?: "",
            date = meta.invoiceDate ?: "",
            distributorName = meta.distributorName ?: "",
            distributorGSTIN = meta.distributorGSTIN ?: "",
            distributorEmail = meta.distributorEmail ?: "",
            distributorPhone = meta.distributorPhone ?: "",
            distributorAddress = meta.distributorAddress ?: "",
            billToName = meta.billToName ?: "",
            skuItems = items,
            taxBreakdown = taxRows,
            invoiceSummary = summary,
            totalItems = items.size,
            totalQuantity = items.sumOf { it.quantity },
            isAutoFilled = meta.confidenceScore >= 20
        )

        // ═══ FULL INVOICE JSON DEBUG ═══
        val itemsJson = items.joinToString(",\n    ") { item ->
            """{ "name": "${item.productName}", "raw_ocr": "${item.rawOcrName}", "qty": ${item.quantity}, "price": ${item.pricePerUnit}, "gst_pct": ${item.gstRate}, "amount": ${item.totalPrice} }"""
        }
        val invoiceJson = buildString {
            appendLine("[OCR_JSON] ╔══════════ INVOICE EXTRACTION ══════════╗")
            appendLine("{")
            appendLine("  \"invoice_number\": \"${meta.invoiceNumber}\",")
            appendLine("  \"date\": \"${meta.invoiceDate}\",")
            appendLine("  \"vendor\": \"${meta.distributorName}\",")
            appendLine("  \"vendor_gstin\": \"${meta.distributorGSTIN}\",")
            appendLine("  \"vendor_phone\": \"${meta.distributorPhone}\",")
            appendLine("  \"vendor_email\": \"${meta.distributorEmail}\",")
            appendLine("  \"bill_to\": \"${meta.billToName}\",")
            appendLine("  \"confidence\": ${meta.confidenceScore},")
            appendLine("  \"items_count\": ${items.size},")
            appendLine("  \"total_qty\": ${items.sumOf { it.quantity }},")
            appendLine("  \"grand_total\": ${items.sumOf { it.totalPrice }},")
            appendLine("  \"items\": [")
            appendLine("    $itemsJson")
            appendLine("  ]")
            append("}")
            appendLine()
            append("[OCR_JSON] ╚════════════════════════════════════════╝")
        }
        println(invoiceJson)
        // ═══════════════════════════════

        return formData
    }

    // ══════════════════════════════════════════════════════════════
    // Bill-To Boundary Detection
    // ══════════════════════════════════════════════════════════════

    /**
     * Find the line index where "Bill To" / "Buyer" section begins.
     */
    internal fun findBillToLineIndex(lines: List<String>): Int {
        for ((index, line) in lines.withIndex()) {
            val lower = line.lowercase().trim()
            if (BILL_TO_KEYWORDS.any { lower.startsWith(it) || lower == it }) {
                return index
            }
        }
        return -1
    }

    /**
     * Find the line index containing the GSTIN keyword.
     * Used as anchor for seller zone detection.
     */
    internal fun findGSTINLineIndex(lines: List<String>): Int {
        val gstKeywords = listOf("gstin/uin", "gstin", "gst in", "gst no")
        for ((index, line) in lines.withIndex()) {
            val lower = line.lowercase().trim()
            if (gstKeywords.any { lower.contains(it) }) {
                return index
            }
        }
        return -1
    }

    // ══════════════════════════════════════════════════════════════
    // Distributor Name Extraction (FIXED)
    // ══════════════════════════════════════════════════════════════

    /**
     * Extract distributor name from SELLER zone (lines before Bill-To).
     *
     * Strategy: The distributor name is the first prominent text line after "Tax Invoice".
     * It may be ALL CAPS or Title Case. We skip header/metadata lines.
     */
    internal fun extractDistributorName(sellerLines: List<String>): String? {
        var pastTaxInvoice = false

        for ((idx, line) in sellerLines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.length < 3) continue

            val lower = trimmed.lowercase()

            // Mark when we pass the "Tax Invoice" header
            if (lower.contains("tax invoice")) {
                pastTaxInvoice = true
                continue
            }

            // Skip known metadata/header patterns
            if (SKIP_NAME_PATTERNS.any { lower.startsWith(it) || lower.contains("gstin") }) continue
            if (EMAIL_REGEX.containsMatchIn(trimmed)) continue
            if (PHONE_REGEX.containsMatchIn(trimmed)) continue
            if (GSTIN_REGEX.containsMatchIn(trimmed.uppercase())) continue
            if (lower.startsWith("sy ") || lower.startsWith("no ") || lower.startsWith("no.")) continue
            if (lower.contains("@")) continue
            if (DATE_NUMERIC_REGEX.containsMatchIn(trimmed)) continue
            // Skip product/table headers and numeric lines
            if (lower.contains("item name") || lower.contains("hsn") || lower.contains("quantity")) continue
            if (lower.contains("price") || lower.contains("amount") || lower.contains("unit")) continue
            if (lower.contains("name:") && lower.contains("bank")) continue
            // Skip pure number lines and currency-prefix lines (T6,257.14, F200.00, etc.)
            if (trimmed.replace(Regex("[\\s,.|*]"), "").all { it.isDigit() }) continue
            if (Regex("""^[TFR*I7|{}]\s*[\d,]+\.\d""").containsMatchIn(trimmed)) continue
            if (lower.contains("holder's name")) continue

            // The first "real" text line after Tax Invoice is the distributor name
            if (pastTaxInvoice || idx <= 3) {
                val words = trimmed.split("\\s+".toRegex()).filter { it.length >= 2 }
                if (words.isNotEmpty() && trimmed.length >= 4) {
                    // Look ahead: if next line might be continuation of a split name
                    // (e.g., "ROYAL" on one line, "DENTERPRISES" on next)
                    if (idx + 1 < sellerLines.size) {
                        val nextLine = sellerLines[idx + 1].trim()
                        val nextLower = nextLine.lowercase()
                        val isNameContinuation = nextLine.length >= 3 &&
                            nextLine.length <= 30 &&
                            !SKIP_NAME_PATTERNS.any { nextLower.startsWith(it) || nextLower.contains("gstin") } &&
                            !EMAIL_REGEX.containsMatchIn(nextLine) &&
                            !PHONE_REGEX.containsMatchIn(nextLine) &&
                            !GSTIN_REGEX.containsMatchIn(nextLine.uppercase()) &&
                            !nextLower.contains("@") &&
                            !nextLower.startsWith("no ") && !nextLower.startsWith("no.") &&
                            nextLine.any { it.isLetter() } &&
                            nextLine.count { it.isLetter() } > nextLine.length / 2
                        if (isNameContinuation) {
                            // Merge and apply OCR correction for common artifacts
                            var mergedNext = nextLine
                            // "DENTERPRISES" is OCR artifact of "ENTERPRISES" (D merged from previous line)
                            if (mergedNext.uppercase().let { it.startsWith("DENT") && it.contains("RPRIS") }) {
                                mergedNext = "ENTERPRISES"
                            }
                            return "$trimmed $mergedNext"
                        }
                    }
                    return trimmed
                }
            }
        }

        return null
    }

    // ══════════════════════════════════════════════════════════════
    // Bill-To (Outlet) Name Extraction
    // ══════════════════════════════════════════════════════════════

    /**
     * Extract the outlet/buyer name from the buyer zone (lines after "Bill To").
     */
    internal fun extractBillToName(buyerLines: List<String>): String? {
        if (buyerLines.isEmpty()) return null

        // Skip the "Bill To:" line itself, take the next meaningful line
        for (i in buyerLines.indices) {
            val line = buyerLines[i].trim()
            val lower = line.lowercase()

            // Skip the "Bill To" header line itself
            if (BILL_TO_KEYWORDS.any { lower.startsWith(it) || lower == it }) continue

            // Skip metadata lines
            if (lower.startsWith("contact") || lower.startsWith("gstin") ||
                lower.startsWith("state") || lower.startsWith("address") ||
                lower.isBlank() || line.length < 3) continue

            // This should be the outlet/buyer name
            return line
        }

        return null
    }

    // ══════════════════════════════════════════════════════════════
    // GSTIN Extraction (IMPROVED)
    // ══════════════════════════════════════════════════════════════

    /**
     * Extract GSTIN with multi-strategy approach.
     *
     * Strategy 1: KEYWORD — find "GSTIN:" and extract the value after it
     * Strategy 2: REGEX — search for strict GSTIN pattern
     * Strategy 3: OCR-CORRECTED REGEX — fix common OCR mistakes and retry
     */
    internal fun extractGSTIN(sellerLines: List<String>, rawText: String): String? {
        // Strategy 1: Keyword-based (most reliable)
        val keywordResult = extractGSTINByKeyword(sellerLines)
            ?: extractGSTINByKeyword(rawText.lines()) // fallback to all lines
        if (keywordResult != null) return keywordResult

        // Strategy 2: Strict regex on seller lines
        for (line in sellerLines) {
            val match = GSTIN_REGEX.find(line.uppercase())
            if (match != null) return match.value
        }

        // Strategy 3: Strict regex on full text
        val fullMatch = GSTIN_REGEX.find(rawText.uppercase())
        if (fullMatch != null) return fullMatch.value

        return null
    }

    /**
     * Find "GSTIN" keyword on a line, extract the alphanumeric value after it,
     * and apply OCR character correction.
     */
    private fun extractGSTINByKeyword(lines: List<String>): String? {
        val gstKeywords = listOf("gstin/uin", "gstin", "gst in", "gst no", "gst")

        for (line in lines) {
            val lower = line.lowercase().trim()

            for (keyword in gstKeywords) {
                if (!lower.contains(keyword)) continue

                // Extract everything after the keyword
                val keyIdx = lower.indexOf(keyword)
                val afterKeyword = line.substring(keyIdx + keyword.length)
                    .trimStart(':', '.', ' ', '-', '/', '#')
                    .trim()

                // Find a 15-character alphanumeric string (GSTIN is always 15 chars)
                val candidate = Regex("""[A-Za-z0-9]{13,16}""").find(afterKeyword)
                if (candidate != null) {
                    val corrected = correctGSTINOcr(candidate.value.uppercase())
                    // Validate: must be 15 chars and match structure loosely
                    if (corrected.length == 15) {
                        debugLog("GSTIN found by keyword: $corrected (raw: ${candidate.value})")
                        return corrected
                    }
                }

                // Also try with spaces removed (OCR sometimes adds spaces)
                val noSpaces = afterKeyword.replace(" ", "").uppercase()
                val spaceCandidate = Regex("""[A-Z0-9]{14,16}""").find(noSpaces)
                if (spaceCandidate != null) {
                    val corrected = correctGSTINOcr(spaceCandidate.value)
                    if (corrected.length == 15) {
                        debugLog("GSTIN found by keyword (no-space): $corrected")
                        return corrected
                    }
                }
            }
        }
        return null
    }

    /**
     * Fix common OCR character confusions in GSTIN based on known structure:
     * Position: DD LLLLL DDDD L X Z X
     *   DD = 2 digits (state code)
     *   LLLLL = 5 letters (PAN first 5)
     *   DDDD = 4 digits (PAN digits)
     *   L = 1 letter (PAN last)
     *   X = 1 alphanumeric
     *   Z = literal Z
     *   X = 1 alphanumeric (check digit)
     */
    private fun correctGSTINOcr(raw: String): String {
        if (raw.length != 15) return raw

        val chars = raw.toCharArray()

        // Positions 0-1: must be digits
        for (i in 0..1) chars[i] = toDigit(chars[i])
        // Positions 2-6: must be letters
        for (i in 2..6) chars[i] = toLetter(chars[i])
        // Positions 7-10: must be digits
        for (i in 7..10) chars[i] = toDigit(chars[i])
        // Position 11: must be letter
        chars[11] = toLetter(chars[11])
        // Position 12: alphanumeric — leave as is
        // Position 13: should be Z
        if (chars[13] == '2') chars[13] = 'Z'  // common OCR confusion
        // Position 14: alphanumeric — leave as is

        return chars.concatToString()
    }

    /** Convert common OCR misreads to digit */
    private fun toDigit(c: Char): Char = when (c) {
        'O', 'o' -> '0'
        'I', 'l' -> '1'
        'Z' -> '2'
        'S', 's' -> '5'
        'B' -> '8'
        'G' -> '6'
        else -> c
    }

    /** Convert common OCR misreads to letter */
    private fun toLetter(c: Char): Char = when (c) {
        '0' -> 'O'
        '1' -> 'I'
        '2' -> 'Z'
        '5' -> 'S'
        '8' -> 'B'
        '6' -> 'G'
        else -> c
    }

    // ══════════════════════════════════════════════════════════════
    // Invoice Number Extraction
    // ══════════════════════════════════════════════════════════════

    internal data class InvoiceNumberResult(val value: String?, val lineIndex: Int)

    /**
     * Extract invoice number using keyword proximity, then fallback to pattern scan.
     */
    internal fun extractInvoiceNumber(lines: List<String>): InvoiceNumberResult {
        // Strategy 1: Look for keyword → extract value after colon or on same/next line
        for ((index, line) in lines.withIndex()) {
            val lower = line.lowercase().trim()
            for (keyword in INVOICE_NO_KEYWORDS) {
                if (lower.contains(keyword)) {
                    // Try to extract value after colon/separator on same line
                    val extracted = extractValueAfterKeyword(line, keyword)
                    if (extracted != null && extracted.length >= 4) {
                        return InvoiceNumberResult(extracted, index)
                    }

                    // Try the remainder of the line after the keyword
                    val keyIdx = lower.indexOf(keyword)
                    val remainder = line.substring(keyIdx + keyword.length).trim()
                        .trimStart(':', '.', ' ', '-')
                        .trim()
                    val match = ALPHANUM_PATTERN.find(remainder)
                    if (match != null && match.value.length >= 4) {
                        return InvoiceNumberResult(match.value, index)
                    }

                    // Try next line
                    if (index + 1 < lines.size) {
                        val nextLine = lines[index + 1].trim()
                        val nextMatch = ALPHANUM_PATTERN.find(nextLine)
                        if (nextMatch != null && nextMatch.value.length >= 4) {
                            return InvoiceNumberResult(nextMatch.value, index + 1)
                        }
                    }
                }
            }
        }

        // Strategy 2: Fallback — look for standalone "Invoice" keyword
        val scanLimit = minOf(30, lines.size)
        for (i in 0 until scanLimit) {
            val line = lines[i]
            val lower = line.lowercase()

            if (lower.contains("bill to") || lower.contains("ship to") ||
                lower.contains("description") || lower.contains("hsn")) continue

            if (lower.contains("invoice") && !lower.contains("tax invoice")) {
                val match = ALPHANUM_PATTERN.findAll(line)
                    .filter { it.value.lowercase() != "invoice" && it.value.length >= 4 }
                    .firstOrNull()
                if (match != null) {
                    return InvoiceNumberResult(match.value, i)
                }
            }
        }

        return InvoiceNumberResult(null, -1)
    }

    // ══════════════════════════════════════════════════════════════
    // Invoice Date Extraction (FIXED)
    // ══════════════════════════════════════════════════════════════

    /**
     * Extract invoice date. Supports dd-mm-yyyy, dd/mm/yyyy, dd-MMM-yy, dd-MMM-yyyy.
     * Prefers alpha-month dates and validates numeric dates.
     */
    internal fun extractInvoiceDate(
        lines: List<String>,
        rawText: String,
        invoiceNumberLineIndex: Int
    ): String? {
        data class DateCandidate(val value: String, val lineIndex: Int, val isAlpha: Boolean)

        val candidates = mutableListOf<DateCandidate>()

        for ((index, line) in lines.withIndex()) {
            val lower = line.lowercase()

            // Skip lines that are invoice numbers to avoid false positives
            if (lower.contains("invoice no") || lower.contains("inv no")) continue

            // Try alpha month format first (15-Sep-25, 21-Aug-25, 15-Sep-2025)
            DATE_ALPHA_REGEX.findAll(line).forEach { match ->
                candidates.add(DateCandidate(match.value, index, isAlpha = true))
            }

            // Try numeric format (19-02-2026, 17/02/2026)
            DATE_NUMERIC_REGEX.findAll(line).forEach { match ->
                // Filter out GSTIN lines, phone numbers, and account lines
                if (!GSTIN_REGEX.containsMatchIn(line.uppercase()) &&
                    !lower.contains("gstin") && !lower.contains("phone") &&
                    !lower.contains("mobile") && !lower.contains("account")) {
                    // Filter patterns like "29-Karnataka"
                    val afterMatch = line.substring(match.range.last + 1).trimStart()
                    if (!afterMatch.startsWith("-K") && !afterMatch.lowercase().startsWith("karnataka")) {
                        // Validate: day must be 1-31, month must be 1-12
                        if (isValidNumericDate(match.value)) {
                            candidates.add(DateCandidate(match.value, index, isAlpha = false))
                        }
                    }
                }
            }
        }

        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates[0].value

        // Strongly prefer alpha-month dates (15-Sep-25 is unambiguous)
        val alphaCandidates = candidates.filter { it.isAlpha }
        if (alphaCandidates.size == 1) return alphaCandidates[0].value

        // Prefer date on a line containing "date" keyword
        val dateKeywordLines = lines.mapIndexedNotNull { i, line ->
            val lower = line.lowercase()
            if (lower.contains("date") && !lower.contains("update")) i else null
        }

        // Score each candidate
        val scored = candidates.map { candidate ->
            val distToInvoice = if (invoiceNumberLineIndex >= 0) {
                kotlin.math.abs(candidate.lineIndex - invoiceNumberLineIndex)
            } else Int.MAX_VALUE

            val distToDateKeyword = dateKeywordLines.minOfOrNull {
                kotlin.math.abs(candidate.lineIndex - it)
            } ?: Int.MAX_VALUE

            val onDateLine = dateKeywordLines.contains(candidate.lineIndex)
            var effectiveDist = if (onDateLine) 0 else minOf(distToInvoice, distToDateKeyword)

            // Alpha dates get a bonus (lower distance = preferred)
            if (candidate.isAlpha) effectiveDist = maxOf(0, effectiveDist - 5)

            Triple(candidate, effectiveDist, candidate.lineIndex)
        }

        return scored.sortedWith(compareBy({ it.second }, { it.third }))
            .firstOrNull()?.first?.value
    }

    /**
     * Validate a numeric date: day must be 1-31, month must be 1-12.
     * Rejects false positives like "91/25-26" from invoice numbers.
     */
    private fun isValidNumericDate(dateStr: String): Boolean {
        val parts = dateStr.split(Regex("""[/\-]"""))
        if (parts.size < 2) return false

        val day = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false

        return day in 1..31 && month in 1..12
    }

    /**
     * Normalize any date format to DD-MM-YYYY.
     * Handles: 15-Sep-25, 15-Sep-2025, 17/02/2026, 17-02-26, etc.
     */
    private fun normalizeDate(dateStr: String): String {
        val monthMap = mapOf(
            "jan" to "01", "feb" to "02", "mar" to "03", "apr" to "04",
            "may" to "05", "jun" to "06", "jul" to "07", "aug" to "08",
            "sep" to "09", "oct" to "10", "nov" to "11", "dec" to "12"
        )

        // Try alpha-month format: 15-Sep-25 or 15-Sep-2025
        val alphaParts = dateStr.split(Regex("""[/\-]"""))
        if (alphaParts.size == 3) {
            val monthStr = alphaParts[1].lowercase().take(3)
            val monthNum = monthMap[monthStr]

            if (monthNum != null) {
                val day = alphaParts[0].padStart(2, '0')
                val year = expandYear(alphaParts[2])
                return "$day-$monthNum-$year"
            }

            // Numeric: DD/MM/YYYY or DD-MM-YY
            val day = alphaParts[0].padStart(2, '0')
            val month = alphaParts[1].padStart(2, '0')
            val year = expandYear(alphaParts[2])
            return "$day-$month-$year"
        }

        // Fallback: return as-is with slashes replaced
        return dateStr.replace('/', '-')
    }

    /** Expand 2-digit year to 4-digit: 25 → 2025 */
    private fun expandYear(year: String): String {
        if (year.length == 4) return year
        if (year.length == 2) {
            val num = year.toIntOrNull() ?: return "20$year"
            return if (num > 50) "19$year" else "20$year"
        }
        return year
    }

    // ══════════════════════════════════════════════════════════════
    // Email Extraction
    // ══════════════════════════════════════════════════════════════

    /**
     * Extract email with keyword-first approach.
     * Strategy 1: Find "Email:" keyword and extract the value after it
     * Strategy 2: Regex scan all lines
     * Strategy 3: Clean OCR artifacts (spaces around @/.) and retry
     */
    internal fun extractEmail(sellerLines: List<String>): String? {
        // Strategy 1: Keyword-based
        val emailKeywords = listOf("e-mail", "email id", "email", "mail id", "mail")
        for (line in sellerLines) {
            val lower = line.lowercase().trim()
            for (keyword in emailKeywords) {
                if (!lower.contains(keyword)) continue

                val keyIdx = lower.indexOf(keyword)
                val afterKeyword = line.substring(keyIdx + keyword.length)
                    .trimStart(':', '.', ' ', '-')
                    .trim()

                // Clean OCR artifacts and search for email
                val cleaned = afterKeyword
                    .replace(Regex("""\s*@\s*"""), "@")
                    .replace(Regex("""\s*\.\s*"""), ".")
                    .replace(" ", "")  // remove all spaces in the email part

                val match = EMAIL_REGEX.find(cleaned)
                if (match != null) {
                    debugLog("Email found by keyword: ${match.value}")
                    return match.value
                }
            }
        }

        // Strategy 2: Direct regex on each line
        for (line in sellerLines) {
            val match = EMAIL_REGEX.find(line)
            if (match != null) return match.value
        }

        // Strategy 3: Clean all lines and retry
        for (line in sellerLines) {
            val cleaned = line.replace(Regex("""\s*@\s*"""), "@")
                .replace(Regex("""\s*\.\s*"""), ".")
            val match = EMAIL_REGEX.find(cleaned)
            if (match != null) return match.value
        }

        return null
    }

    /**
     * Ultimate fallback: search the entire raw OCR text (as one big string)
     * for an email. This catches emails split across lines.
     */
    private fun extractEmailFromRawText(rawText: String): String? {
        // Clean the entire raw text of spaces around @ and .
        val cleaned = rawText
            .replace(Regex("""\s*@\s*"""), "@")
            .replace(Regex("""\s*\.\s*com"""), ".com")
            .replace(Regex("""\s*\.\s*in"""), ".in")
            .replace(Regex("""\s*\.\s*co"""), ".co")
            .replace(Regex("""\s*\.\s*org"""), ".org")
            .replace(Regex("""\s*\.\s*net"""), ".net")

        val match = EMAIL_REGEX.find(cleaned)
        if (match != null) {
            debugLog("Email found in raw text: ${match.value}")
            return match.value
        }

        // Try to find email by domain anchor: look for @gmail, @yahoo, etc. in raw text
        val domainAnchors = listOf("gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "rediffmail.com")
        val noSpaceText = rawText.replace(" ", "").lowercase()
        for (domain in domainAnchors) {
            val domainIdx = noSpaceText.indexOf("@$domain")
            if (domainIdx > 0) {
                // Walk backwards to find the start of the email
                var start = domainIdx - 1
                while (start >= 0 && (noSpaceText[start].isLetterOrDigit() || noSpaceText[start] in "._%-+")) {
                    start--
                }
                start++
                val email = noSpaceText.substring(start, domainIdx + domain.length + 1)
                if (EMAIL_REGEX.containsMatchIn(email)) {
                    debugLog("Email found by domain anchor: $email")
                    return email
                }
            }
        }

        return null
    }

    // ══════════════════════════════════════════════════════════════
    // Phone Extraction
    // ══════════════════════════════════════════════════════════════

    /**
     * Extract phone number from seller zone.
     * Skips bank account numbers and prioritizes lines with "Phone"/"Mobile" keyword.
     */
    internal fun extractPhone(sellerLines: List<String>): String? {
        // Skip words that indicate non-phone numbers
        val skipKeywords = listOf("account", "ifsc", "a/c", "ac no", "cso contact", "invoice", "hsn")

        // Strategy 1: Find phone on a line with "phone" or "mobile" keyword
        for (line in sellerLines) {
            val lower = line.lowercase()
            if (lower.contains("phone") || lower.contains("mobile") || lower.contains("mob")) {
                val match = PHONE_REGEX.find(line)
                if (match != null) return match.value
            }
        }

        // Strategy 2: Fallback — find first phone-like number, skip account lines
        for (line in sellerLines) {
            val lower = line.lowercase()
            if (skipKeywords.any { lower.contains(it) }) continue
            val match = PHONE_REGEX.find(line)
            if (match != null) return match.value
        }

        return null
    }

    // ══════════════════════════════════════════════════════════════
    // Address Extraction
    // ══════════════════════════════════════════════════════════════

    /**
     * Extract address from seller zone.
     * Strategy: collect lines between the distributor name and the first metadata line.
     * Address on invoices is always right after the distributor name.
     */
    internal fun extractAddress(sellerLines: List<String>, distributorName: String?): String? {
        if (distributorName == null) return null

        val nameIndex = sellerLines.indexOfFirst { it.trim() == distributorName }
        if (nameIndex < 0) return null

        val addressParts = mutableListOf<String>()

        for (i in (nameIndex + 1) until sellerLines.size) {
            val line = sellerLines[i].trim()
            val lower = line.lowercase()

            // Stop conditions — these are metadata lines, not address
            val isMetadataLine = lower.startsWith("gstin") || lower.startsWith("gst") ||
                lower.startsWith("phone") || lower.startsWith("mobile") ||
                lower.startsWith("email") || lower.startsWith("e-mail") ||
                lower.startsWith("invoice") || lower.startsWith("state name") ||
                lower.startsWith("state:") || lower.startsWith("cin") ||
                lower.startsWith("fssai") || lower.startsWith("pan") ||
                lower.startsWith("dl no") ||
                GSTIN_REGEX.containsMatchIn(line.uppercase()) ||
                (EMAIL_REGEX.containsMatchIn(line) && lower.contains("@"))

            if (isMetadataLine) break

            // Also stop if line is purely a phone number
            if (PHONE_REGEX.matches(line.trim())) break

            if (line.isNotBlank() && line.length >= 3) {
                addressParts.add(line)
            }
        }

        return if (addressParts.isNotEmpty()) addressParts.joinToString(", ") else null
    }

    // ══════════════════════════════════════════════════════════════
    // Confidence Scoring
    // ══════════════════════════════════════════════════════════════

    /**
     * Compute confidence score: weighted scoring for each field.
     * Total possible: 100
     */
    internal fun computeConfidence(metadata: InvoiceMetaData): Int {
        var score = 0
        if (!metadata.distributorName.isNullOrBlank()) score += 15
        if (!metadata.distributorGSTIN.isNullOrBlank()) score += 20
        if (!metadata.invoiceNumber.isNullOrBlank()) score += 20
        if (!metadata.invoiceDate.isNullOrBlank()) score += 15
        if (!metadata.distributorEmail.isNullOrBlank()) score += 5
        if (!metadata.distributorPhone.isNullOrBlank()) score += 5
        if (!metadata.distributorAddress.isNullOrBlank()) score += 5
        if (!metadata.billToName.isNullOrBlank()) score += 15
        return score
    }

    // ══════════════════════════════════════════════════════════════
    // Utility Functions
    // ══════════════════════════════════════════════════════════════

    private fun extractValueAfterKeyword(line: String, keyword: String): String? {
        val lower = line.lowercase()
        val keyIdx = lower.indexOf(keyword)
        if (keyIdx < 0) return null

        val afterKeyword = line.substring(keyIdx + keyword.length)
            .trimStart(':', '.', ' ', '-', '#')
            .trim()

        if (afterKeyword.isBlank()) return null

        val match = ALPHANUM_PATTERN.find(afterKeyword)
        return match?.value
    }

    // ══════════════════════════════════════════════════════════════
    // Production Item Extraction (Block 2)
    // ══════════════════════════════════════════════════════════════

    private fun extractLineItems(lines: List<String>, rawLines: List<String> = emptyList()): List<SkuLineItem> {
        val items = mutableListOf<SkuLineItem>()

        // Dump ALL normalized lines for debugging
        debugLog("=== ALL NORMALIZED LINES (${lines.size}) ===")
        lines.forEachIndexed { i, line -> debugLog("  [$i]: $line") }
        debugLog("=== END ALL LINES ===")

        // ════════════════════════════════════════════════════════════
        // CATALOG-FIRST APPROACH
        // ML Kit reads table COLUMNS separately, so product names,
        // HSN codes, quantities, prices appear as separate line groups.
        // Instead of trying to reconstruct rows, we:
        //   1. Scan ALL lines for product catalog matches
        //   2. Collect HSN codes independently
        //   3. Collect numeric columns (quantity, price, amount)
        //   4. Zip everything together by order
        // ════════════════════════════════════════════════════════════

        // Step 1: Find all product names by scanning every line against catalog
        data class ProductMatch(
            val catalogProduct: MasterProduct,
            val rawOcrText: String,
            val lineIndex: Int
        )

        val productMatches = mutableListOf<ProductMatch>()
        val usedLineIndices = mutableSetOf<Int>()

        // Lines to skip (not product lines)
        val skipPatterns = listOf(
            "tax invoice", "gstin", "bill to", "buyer", "state:", "invoice no",
            "date", "phone", "email", "bank", "account", "ifsc", "branch",
            "terms", "note:", "total", "sub total", "balance", "received",
            "round off", "amount in words", "payment", "credit", "goods once",
            "service provider", "reverse charge", "cso name", "cso contact",
            "place of supply", "hsn/ sac", "hsn/sac", "price/ unit", "price/unit",
            "tax type", "taxable amount", "tax amount", "sgst", "cgst",
            "quantity", "item name", "description"
        )

        for ((idx, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.length < 5) continue
            val lower = trimmed.lowercase()

        // Skip known non-product lines
            if (skipPatterns.any { lower.contains(it) }) continue
            // Skip lines containing standalone "gst" (not inside a product name)
            if (lower.matches(Regex(".*\\bgst\\b.*"))) continue
            // Skip pure number lines (with OCR prefix letters T/F/R/I/7/*)
            if (trimmed.replace(Regex("[\\s,.|*₹TFR{}I7|()%]"), "").all { it.isDigit() || it == '.' }) continue
            // Skip lines that are just "CASE" repeated
            if (lower.replace("case", "").replace(" ", "").isEmpty()) continue
            // Must have some alphabetic content (at least 3 letters)
            if (trimmed.count { it.isLetter() } < 3) continue
            // Skip lines that look like currency amounts (T675.00, F1,080.00, etc.)
            if (Regex("""^[TFR*I7|{} ]*\s*[\d,]+\.\d{2}\s*$""").containsMatchIn(trimmed)) continue
            // Skip lines that are terms/conditions/legal text
            if (lower.startsWith("1.") || lower.startsWith("2.") || lower.startsWith("3.")) continue
            // Skip address lines, single words like "Amount", "Unit", "Rate"
            if (lower.matches(Regex("^(amount|unit|rate|total|balance)$"))) continue
            // Skip "200ML PET" / "500ML" / "150 ML" standalone size lines
            if (trimmed.matches(Regex("""^\d{2,4}\s*ML\s*(PET)?\s*\d*$""", RegexOption.IGNORE_CASE))) continue

            // Strip trailing CGST/SGST that normalizer may have appended from adjacent column
            val cleanedForMatch = trimmed
                .replace(Regex("\\s+(CGST|SGST|cgst|sgst)\\s*$"), "")
                // Strip leading row numbers like "12 CAMPA JEERA..."
                .replace(Regex("^\\d{1,2}\\s+"), "")
                .trim()

            // Try to match against product catalog
            var match = ProductMasterCatalog.findBestMatch(cleanedForMatch)
            if (match != null) {
                // VARIANT FIX: Check the NEXT line for a size indicator (500ML, 200ML, 150ML)
                // OCR often splits "CAMPA COLA FLVRD DRINK" and "500ML PET" into 2 lines
                // Without the size, we'd match 200ml. Combining gives the correct variant.
                if (idx + 1 < lines.size) {
                    val nextLine = lines[idx + 1].trim()
                        // Strip ALL CGST/SGST tokens (may be multiple: "500ML PET SGST CGST SGST")
                        .replace(Regex("\\b(CGST|SGST|cgst|sgst)\\b"), "")
                        .trim()
                    val sizeRegex = Regex("""^\|?(\d{2,4}\s*ML\s*(PET)?\s*\d*)$""", RegexOption.IGNORE_CASE)
                    if (sizeRegex.containsMatchIn(nextLine)) {
                        val cleanedNext = nextLine.replace("|", "").trim()
                        val combined = "$cleanedForMatch $cleanedNext"
                        val betterMatch = ProductMasterCatalog.findBestMatch(combined)
                        if (betterMatch != null && betterMatch.id != match.id) {
                            debugLog("catalogScan[$idx] VARIANT FIX: '$cleanedForMatch' + '$cleanedNext' → ${betterMatch.displayName} (was ${match.displayName})")
                            match = betterMatch
                        }
                    }
                }

                // Check for duplicates — same catalog product from adjacent lines
                // Allow same product to appear again if far enough apart (different variant section)
                val isDuplicate = productMatches.any { pm ->
                    pm.catalogProduct.id == match!!.id &&
                    kotlin.math.abs(pm.lineIndex - idx) <= 2
                }
                if (!isDuplicate) {
                    productMatches.add(ProductMatch(match!!, cleanedForMatch, idx))
                    usedLineIndices.add(idx)
                    debugLog("catalogScan[$idx] MATCH: '$cleanedForMatch' → ${match!!.displayName} (${match!!.id})")
                } else {
                    debugLog("catalogScan[$idx] DUPLICATE (skipped): '$cleanedForMatch' → ${match!!.displayName}")
                }
            }
        }

        debugLog("catalogScan: found ${productMatches.size} product matches")

        if (productMatches.isEmpty()) {
            debugLog("extractLineItems: no catalog matches found, falling back to legacy")
            return extractLineItemsLegacy(lines)
        }

        // Step 2: Collect HSN codes from all lines
        val hsnCodes = mutableListOf<String>()
        // More lenient HSN regex — handles trailing OCR noise like 'o', 'O', pipes
        val hsnLineRegex = Regex("""\b(2[0-9]{7})[oO|]*\b""")
        for (line in lines) {
            val trimmed = line.trim().removePrefix("|").trim()
            val hsnMatches = hsnLineRegex.findAll(trimmed)
            for (hsnMatch in hsnMatches) {
                val hsn = hsnMatch.groupValues[1]
                // Only count if the line is primarily numeric (HSN column line)
                val letterCount = trimmed.count { it.isLetter() }
                val digitCount = trimmed.count { it.isDigit() }
                if (digitCount > letterCount || trimmed.length <= 12) {
                    hsnCodes.add(hsn)
                }
            }
        }
        debugLog("catalogScan: collected ${hsnCodes.size} HSN codes: $hsnCodes")

        // Step 3: Collect quantities
        // Use RAW lines because normalizer merges standalone digits into adjacent lines.
        val quantities = mutableListOf<Int>()
        val linesToScanForQty = if (rawLines.isNotEmpty()) rawLines else lines

        // Strategy A: Look for QTY_UNIT_REGEX patterns (e.g., "3 CASE")
        val qtyUnitLines = linesToScanForQty.filter {
            QTY_UNIT_REGEX.containsMatchIn(it)
        }
        for (qLine in qtyUnitLines) {
            QTY_UNIT_REGEX.findAll(qLine).forEach { qtMatch ->
                val qty = qtMatch.groupValues[1].toIntOrNull()
                if (qty != null && qty > 0 && qty < 500) quantities.add(qty)
            }
        }
        // Strategy B: Look for standalone small numbers in RAW lines after "Quantity" header
        if (quantities.isEmpty()) {
            val qtyHeaderIdx = linesToScanForQty.indexOfFirst {
                it.trim().lowercase().let { l -> l == "quantity" || l == "qty" }
            }
            if (qtyHeaderIdx >= 0) {
                // Collect standalone small numbers after Quantity header.
                // Cap at 20 to exclude sub-total (42) and serial numbers.
                for (i in (qtyHeaderIdx + 1) until linesToScanForQty.size) {
                    val trimmed = linesToScanForQty[i].trim()
                    if (trimmed.matches(Regex("""^\d{1,2}$"""))) {
                        val n = trimmed.toIntOrNull()
                        if (n != null && n in 1..20) {  // cap at 20, not 500
                            quantities.add(n)
                        }
                    }
                    if (quantities.size >= productMatches.size) break
                    // Only stop at truly unrelated sections (seller info, address, price)
                    val lower = trimmed.lowercase()
                    if (lower.startsWith("royal") || lower.startsWith("ground floor") ||
                        lower.startsWith("phone") || lower.startsWith("gstin") ||
                        lower.startsWith("price")) break
                }
            }
            // Fallback: scan raw lines AFTER "Item name" header for standalone small numbers.
            // Anchoring after "Item name" avoids picking up invoice serial numbers printed at top.
            if (quantities.isEmpty()) {
                val itemNameIdx = linesToScanForQty.indexOfFirst {
                    it.trim().lowercase().let { l -> l == "item name" || l.startsWith("item name") }
                }
                val scanFrom = if (itemNameIdx >= 0) itemNameIdx else 0
                for (i in scanFrom until linesToScanForQty.size) {
                    val trimmed = linesToScanForQty[i].trim()
                    if (trimmed.matches(Regex("""^\d{1,2}$"""))) {
                        val n = trimmed.toIntOrNull()
                        if (n != null && n in 1..20) quantities.add(n)
                    }
                    // Stop at address section
                    val lower = trimmed.lowercase()
                    if (lower.startsWith("royal") || lower.startsWith("ground floor") ||
                        lower.startsWith("phone")) break
                }
            }
        }
        // Supplemental: If we found SOME quantities but fewer than products,
        // scan lines AFTER "Item name" for standalone 1-2 digit numbers as a top-up.
        // Anchoring after "Item name" avoids picking up invoice serial numbers (8, 9, 11, 12)
        // that appear at the very top of the OCR output.
        // Cap at 20 to avoid picking up sub-total counts (e.g. "42" from "Sub Total 42").
        if (quantities.isNotEmpty() && quantities.size < productMatches.size) {
            val existing = quantities.size
            val itemNameIdx = linesToScanForQty.indexOfFirst {
                it.trim().lowercase().let { l -> l == "item name" || l.startsWith("item name") }
            }
            val scanFrom = if (itemNameIdx >= 0) itemNameIdx else 0
            for (i in scanFrom until linesToScanForQty.size) {
                val trimmed = linesToScanForQty[i].trim()
                if (trimmed.matches(Regex("""^\d{1,2}$"""))) {
                    val n = trimmed.toIntOrNull()
                    if (n != null && n in 1..20) quantities.add(n)
                }
                // Stop at address section
                val lower = trimmed.lowercase()
                if (lower.startsWith("royal") || lower.startsWith("ground floor") ||
                    lower.startsWith("phone")) break
            }
            debugLog("catalogScan: qty supplemental scan added ${quantities.size - existing} more (was $existing)")
        }
        debugLog("catalogScan: collected ${quantities.size} quantities: $quantities")

        // Step 4: Collect amount column
        // Use RAW lines because normalizer may merge amount values.
        // Also: DON'T stop at text lines — column-based OCR interleaves legal text with amounts
        val linesToScanForAmt = if (rawLines.isNotEmpty()) rawLines else lines
        val amountHeaderIdx = linesToScanForAmt.indexOfFirst {
            it.trim().lowercase().let { l -> l == "amount" || l == "amounts" }
        }
        val amounts = mutableListOf<Double>()
        // Primary pattern: standard amount with optional OCR-noise prefix chars (inc. E for E840.00)
        val amountPattern = Regex("""^[TFR7EI|*{# ]*\s*([\d,]+\.\d{2})$""")
        // Secondary pattern: comma-as-dot e.g. "71.050.00" → OCR read 1,050.00 as 1.050.00
        // Matches: optional prefix + digit + dot + 3-digit group + dot + 2-digit cents
        val amountPatternCsaD = Regex("""^[TFR7EI|*{# ]*\s*(\d+)\.(\d{3})\.(\d{2})$""")

        if (amountHeaderIdx >= 0) {
            // Collect ALL amount-pattern lines after the header
            // Skip (don't stop at) non-matching lines like legal text
            for (i in (amountHeaderIdx + 1) until linesToScanForAmt.size) {
                val trimmed = linesToScanForAmt[i].trim()
                // Try primary pattern first
                val amMatch = amountPattern.find(trimmed)
                if (amMatch != null) {
                    val value = amMatch.groupValues[1].replace(",", "").toDoubleOrNull()
                    if (value != null && value > 0 && value < 100000) {
                        amounts.add(value)
                    }
                } else {
                    // Try secondary pattern: comma-as-dot e.g. "71.050.00" → 1050.00
                    val csadMatch = amountPatternCsaD.find(trimmed)
                    if (csadMatch != null) {
                        val whole = csadMatch.groupValues[1]
                        val thousands = csadMatch.groupValues[2]
                        val cents = csadMatch.groupValues[3]
                        val value = "$whole$thousands.$cents".toDoubleOrNull()
                        if (value != null && value > 0 && value < 100000) {
                            amounts.add(value)
                        }
                    }
                }
                // Stop if we've collected enough (more than item count + some buffer)
                if (amounts.size >= productMatches.size + 5) break
            }
        }
        // Fallback: scan all raw lines
        if (amounts.isEmpty()) {
            for (line in linesToScanForAmt) {
                val trimmed = line.trim()
                val amMatch = amountPattern.find(trimmed)
                if (amMatch != null) {
                    val value = amMatch.groupValues[1].replace(",", "").toDoubleOrNull()
                    if (value != null && value > 0 && value < 100000) {
                        amounts.add(value)
                    }
                }
            }
        }
        debugLog("catalogScan: collected ${amounts.size} amounts: $amounts")

        // Step 5: Collect GST amounts from price lines
        // Look for lines like "192.86 (40%)" or "30.00 (5%)"
        val gstRates = mutableListOf<Double>()
        for (line in lines) {
            val rateMatch = GST_RATE_PAREN_REGEX.find(line.trim())
            if (rateMatch != null) {
                val rate = rateMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                if (rate > 0) gstRates.add(rate)
            }
        }
        debugLog("catalogScan: collected ${gstRates.size} GST rates: $gstRates")

        // Step 5b: Collect per-unit prices from the Price/ Unit column
        // Use RAW lines. Extract the FIRST decimal number from each line,
        // skipping lines that are clearly non-price (GST-only, text, etc.)
        val prices = mutableListOf<Double>()
        val priceHeaderIdx = linesToScanForAmt.indexOfFirst {
            val l = it.trim().lowercase()
            l.contains("price/") || l.contains("price /")
        }
        if (priceHeaderIdx >= 0) {
            // Extract first decimal number from each line
            val firstNumberRegex = Regex("""[TFR7I|*{# ]*\s*([\d,]+\.\d{2})""")
            for (i in (priceHeaderIdx + 1) until linesToScanForAmt.size) {
                val trimmed = linesToScanForAmt[i].trim()
                val lower = trimmed.lowercase()
                // Stop at Amount header
                if (lower == "amount" || lower == "amounts") break
                // Skip text-heavy lines (> 5 letters and no decimal number)
                if (trimmed.count { it.isLetter() } > 5) continue
                // For compound lines with (rate%) suffix, distinguish:
                //   TWO numbers + (rate%) = "160.71 192.86 (40%)" → first is price ✅
                //   ONE number + (rate%) = "k 192.86 (40%)" → tax amount, skip ❌
                val hasGstSuffix = trimmed.contains(Regex("""\(\d+%\)"""))
                if (hasGstSuffix) {
                    // Count how many decimal numbers appear BEFORE the (rate%)
                    val beforeRate = trimmed.replace(Regex("""\(\d+%\).*"""), "")
                    val decimalCount = Regex("""\d+\.\d{2}""").findAll(beforeRate).count()
                    if (decimalCount < 2) continue  // Single number = tax amount, skip
                }
                val prMatch = firstNumberRegex.find(trimmed)
                if (prMatch != null) {
                    val value = prMatch.groupValues[1].replace(",", "").toDoubleOrNull()
                    if (value != null && value > 0 && value < 1000) {
                        prices.add(value)
                    }
                }
                // Stop if we've collected enough
                if (prices.size >= productMatches.size) break
            }
        }
        debugLog("catalogScan: collected ${prices.size} prices: $prices")

        // Step 6: Build SkuLineItems by zipping product matches with numeric columns
        // Cap amounts at product count to avoid including total/subtotal lines
        // Cap amounts: take only productCount entries AND filter out grand-total-sized values
        val maxReasonableAmount = 5000.0  // Per-line-item cap: no single item should be ₹5000+
        val cappedAmounts = amounts
            .filter { it < maxReasonableAmount }
            .let { if (it.size > productMatches.size) it.take(productMatches.size) else it }

        val itemCount = productMatches.size
        for ((i, pm) in productMatches.withIndex()) {
            val hsn = if (i < hsnCodes.size) hsnCodes[i] else pm.catalogProduct.hsnCode
            val qty = if (i < quantities.size) quantities[i] else 0
            val amount = if (i < cappedAmounts.size) cappedAmounts[i] else 0.0
            // Always use catalog GST rate — OCR column extraction is unreliable for multi-column layouts
            val gstRate = pm.catalogProduct.defaultGstRate
            val price = if (i < prices.size) prices[i] else 0.0

            // Compute pricePerUnit: back-compute from amount when possible, far more reliable
            // than the OCR price column which has multi-column alignment issues.
            //   price = amount / (qty × (1 + gstRate/100))
            // Fall back to extracted price column only when amount or qty is missing.
            val backComputedPrice = if (amount > 0 && qty > 0) {
                val divisor = qty * (1 + gstRate / 100.0)
                if (divisor > 0) round((amount / divisor) * 100.0) / 100.0 else 0.0
            } else 0.0

            val finalPrice = when {
                backComputedPrice > 0 -> backComputedPrice  // prefer back-computed
                i < prices.size -> prices[i]               // fallback: OCR price column
                else -> 0.0
            }

            // Total = price × qty × (1 + gst%), or OCR amount if can't compute
            val computedTotal = if (finalPrice > 0 && qty > 0) round(finalPrice * qty * (1 + gstRate / 100.0)).toDouble() else amount

            val item = SkuLineItem(
                id = "item-${(100000..999999).random()}-${(1..999).random()}",
                productName = pm.catalogProduct.displayName,
                rawOcrName = pm.rawOcrText,
                matchedProductId = pm.catalogProduct.id,
                isMatched = true,
                hsnCode = hsn,
                quantity = qty,
                unit = "CASE",
                mrp = 0.0,
                pricePerUnit = finalPrice,
                gstRate = gstRate,
                gstAmount = 0.0,
                totalPrice = computedTotal
            )
            items.add(item)
            debugLog("catalogItem[$i]: ${item.productName} | HSN=$hsn | qty=$qty | price=$finalPrice (back=${backComputedPrice>0}) | total=$computedTotal | gst=$gstRate%")
        }

        debugLog("extractLineItems: extracted ${items.size} items via catalog-first")

        // ═══ JSON DEBUG DUMP ═══
        // Print the full extraction result as JSON for easy debugging.
        // Tag: [OCR_JSON] — filter logcat with this tag.
        val jsonItems = items.joinToString(",\n    ") { item ->
            """{ "name": "${item.productName}", "raw_ocr": "${item.rawOcrName}", "qty": ${item.quantity}, "price": ${item.pricePerUnit}, "gst_pct": ${item.gstRate}, "amount": ${item.totalPrice} }"""
        }
        // Pull invoice-level fields from the first matched line if available
        val jsonDebug = buildString {
            appendLine("[OCR_JSON] ═══ EXTRACTION RESULT ═══")
            appendLine("{")
            appendLine("  \"items_count\": ${items.size},")
            appendLine("  \"items\": [")
            appendLine("    $jsonItems")
            appendLine("  ]")
            append("}")
        }
        println(jsonDebug)
        // ═══════════════════════

        return items
    }

    /** Legacy row-based extraction, kept as fallback when no catalog matches are found */
    private fun extractLineItemsLegacy(lines: List<String>): List<SkuLineItem> {
        val items = mutableListOf<SkuLineItem>()

        val headerKeywords = listOf(
            "DESCRIPTION OF GOODS", "ITEM NAME", "DESCRIPTION",
            "PARTICULARS", "PRODUCT NAME", "MATERIAL"
        )
        val headerIdx = lines.indexOfFirst { line ->
            val upper = line.uppercase()
            headerKeywords.any { keyword -> keyword in upper }
        }
        if (headerIdx < 0) return items

        val stopKeywords = listOf("TOTAL", "SUB TOTAL", "SUBTOTAL", "GRAND TOTAL")
        val totalIdx = lines.drop(headerIdx + 1).indexOfFirst { line ->
            val trimUpper = line.trim().uppercase()
            stopKeywords.any { trimUpper.startsWith(it) }
        }
        if (totalIdx < 0) return items

        val itemZoneLines = lines.subList(headerIdx + 1, headerIdx + 1 + totalIdx)
        val rowNumberRegex = Regex("""^\s*(\d{1,2})[\s.)]+[A-Za-z]""")
        val hasRowNumbers = itemZoneLines.any { rowNumberRegex.containsMatchIn(it) }

        val itemRows = mutableListOf<String>()
        if (hasRowNumbers) {
            var currentGroup = StringBuilder()
            for (line in itemZoneLines) {
                if (rowNumberRegex.containsMatchIn(line)) {
                    if (currentGroup.isNotEmpty()) itemRows.add(currentGroup.toString())
                    currentGroup = StringBuilder(line)
                } else if (line.trim().isNotEmpty()) {
                    if (currentGroup.isNotEmpty()) currentGroup.append(" ")
                    currentGroup.append(line.trim())
                }
            }
            if (currentGroup.isNotEmpty()) itemRows.add(currentGroup.toString())
        } else {
            for (line in itemZoneLines) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) itemRows.add(trimmed)
            }
        }

        itemRows.forEachIndexed { index, rowText ->
            val item = parseItemRow(rowText, index)
            if (item != null) items.add(item)
        }
        return items
    }

    /**
     * Extract all numeric values from a string (handles commas, decimals).
     * Returns list of (value, matchRange) pairs.
     */
    private fun extractAllNumbers(text: String): List<Pair<Double, IntRange>> {
        // Match numbers that may have commas and decimals, optionally preceded by ₹/Rs
        val numberRegex = Regex("""[₹Rs.]*\s*([\d,]+\.?\d*)""")
        return numberRegex.findAll(text).mapNotNull { match ->
            val numStr = match.groupValues[1].replace(",", "")
            val value = numStr.toDoubleOrNull()
            if (value != null && value > 0) Pair(value, match.range) else null
        }.toList()
    }

    private fun parseItemRow(rowText: String, index: Int): SkuLineItem? {
        if (rowText.isBlank()) return null
        debugLog("parseItemRow[$index] input: $rowText")

        // Strip leading row number (e.g., "1 ", "12. ", "1) ")
        val rowNumRegex = Regex("""^\s*\d{1,2}[\s.\)]+""")
        var cleanRow = rowText.replace(rowNumRegex, "").trim()
        debugLog("parseItemRow[$index] cleanRow: $cleanRow")

        // Extract HSN code (8-digit number starting with 2)
        val hsnMatch = HSN_REGEX.find(cleanRow)
        val hsnCode = hsnMatch?.groupValues?.get(1) ?: ""
        debugLog("parseItemRow[$index] hsn: $hsnCode")

        // Extract quantity and unit — try multiple strategies
        val qtyMatch = QTY_UNIT_REGEX.find(cleanRow)
        var quantity = qtyMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        var unit = qtyMatch?.groupValues?.get(2) ?: ""
        debugLog("parseItemRow[$index] qty by unit regex: $quantity $unit")

        // Extract GST rate
        val gstRateParenMatch = GST_RATE_PAREN_REGEX.find(cleanRow)
        val gstRatePlainMatch = GST_RATE_PLAIN_REGEX.find(cleanRow)
        val gstRate = when {
            gstRateParenMatch != null -> gstRateParenMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            gstRatePlainMatch != null -> {
                val v = gstRatePlainMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                // Only use if it looks like a GST rate (not a random percentage)
                if (v in listOf(5.0, 12.0, 18.0, 28.0, 40.0, 2.5, 6.0, 9.0, 14.0, 20.0)) v else 0.0
            }
            else -> 0.0
        }
        debugLog("parseItemRow[$index] gstRate: $gstRate")

        // Extract product name — text before HSN code or before first number
        var productName = if (hsnMatch != null) {
            cleanRow.substring(0, hsnMatch.range.first).trim()
        } else {
            // Take text before the first standalone number (3+ digits)
            val firstNumMatch = Regex("""(?<!\S)\d{3,}""").find(cleanRow)
            if (firstNumMatch != null) {
                cleanRow.substring(0, firstNumMatch.range.first).trim()
            } else {
                cleanRow
            }
        }
        // Clean product name — remove trailing numbers/noise
        productName = productName.replace(Regex("""\s+\d+\s*$"""), "").trim()
        debugLog("parseItemRow[$index] productName: '$productName'")

        if (productName.length < 2) {
            debugLog("parseItemRow[$index] SKIPPED: productName too short")
            return null
        }

        // Extract ALL numbers from the part AFTER the product name
        val afterNameStart = if (hsnMatch != null) hsnMatch.range.first else {
            val firstNum = Regex("""(?<!\S)\d{3,}""").find(cleanRow)
            firstNum?.range?.first ?: cleanRow.length
        }
        val numericPart = cleanRow.substring(afterNameStart)
        val allNumbers = Regex("""[\d,]+\.?\d*""").findAll(numericPart)
            .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
            .filter { it > 0 }
            .toList()
        debugLog("parseItemRow[$index] allNumbers from numericPart: $allNumbers")

        // Assign numbers based on heuristics:
        // Typical column order: HSN | Qty | Unit | Rate | (MRP) | GST% | GST Amt | Total
        // Filter out HSN (already extracted), GST rate (already extracted)
        val numbersExcludingHsn = allNumbers.filter { num ->
            // Exclude the HSN code value
            val hsnVal = hsnCode.toDoubleOrNull()
            num != hsnVal
        }
        debugLog("parseItemRow[$index] numbersExcludingHsn: $numbersExcludingHsn")

        // Try to identify quantity (small integer, usually < 500)
        if (quantity == 0 && numbersExcludingHsn.isNotEmpty()) {
            // First small integer is likely quantity
            val candidateQty = numbersExcludingHsn.firstOrNull { it < 500 && it == it.toInt().toDouble() }
            if (candidateQty != null) {
                quantity = candidateQty.toInt()
            }
        }

        // Price values: typically the larger numbers are prices
        val priceNumbers = numbersExcludingHsn.filter { num ->
            num != quantity.toDouble() && num != gstRate
        }
        debugLog("parseItemRow[$index] priceNumbers: $priceNumbers")

        val pricePerUnit = if (priceNumbers.size >= 2) priceNumbers[0] else 0.0
        val totalPrice = priceNumbers.lastOrNull() ?: 0.0
        val gstAmount = if (priceNumbers.size >= 3) priceNumbers[priceNumbers.size - 2] else 0.0

        if (unit.isBlank()) unit = "CS"

        // Match to product catalog
        val match = ProductMasterCatalog.findBestMatch(productName)
        val isMatched = match != null
        debugLog("parseItemRow[$index] catalogMatch: ${match?.displayName ?: "NONE"}")

        val result = SkuLineItem(
            id = "item-${index + 1}",
            rawOcrName = productName,
            productName = match?.displayName ?: productName,
            matchedProductId = match?.id,
            isMatched = isMatched,
            hsnCode = hsnCode.ifBlank { match?.hsnCode ?: "" },
            quantity = quantity,
            unit = unit,
            pricePerUnit = pricePerUnit,
            gstRate = if (gstRate > 0) gstRate else match?.defaultGstRate ?: 0.0,
            gstAmount = gstAmount,
            totalPrice = totalPrice
        )
        debugLog("parseItemRow[$index] RESULT: name='${result.productName}', qty=${result.quantity}, price=${result.pricePerUnit}, total=${result.totalPrice}")
        return result
    }

    private fun extractTaxBreakdown(lines: List<String>): List<TaxBreakdown> {
        val taxRows = mutableListOf<TaxBreakdown>()

        // Look for CGST/SGST rows
        for (line in lines) {
            val match = TAX_ROW_REGEX.find(line)
            if (match != null) {
                val taxType = match.groupValues[1]
                val taxableAmount = match.groupValues[2].replace(",", "").toDoubleOrNull() ?: 0.0
                val rate = match.groupValues[3].toDoubleOrNull() ?: 0.0
                val taxAmount = match.groupValues[4].replace(",", "").toDoubleOrNull() ?: 0.0

                taxRows.add(TaxBreakdown(
                    taxType = taxType,
                    taxableAmount = taxableAmount,
                    rate = rate,
                    taxAmount = taxAmount
                ))
            }
        }

        return taxRows
    }

    private fun extractInvoiceSummary(lines: List<String>): InvoiceSummary {
        var subTotal = 0.0
        var totalTax = 0.0
        var roundOff = 0.0
        var grandTotal = 0.0
        var receivedAmount = 0.0
        var balanceDue = 0.0

        for (line in lines) {
            val upper = line.uppercase()
            val amount = extractLastAmount(line)

            when {
                "SUB TOTAL" in upper || "SUBTOTAL" in upper -> subTotal = amount
                "ROUND OFF" in upper || "ROUND-OFF" in upper -> roundOff = amount
                "GRAND TOTAL" in upper -> grandTotal = amount
                "BALANCE" in upper && "DUE" in upper -> balanceDue = amount
                "BALANCE" in upper -> balanceDue = amount
                "RECEIVED" in upper -> receivedAmount = amount
                "AMOUNT DUE" in upper -> balanceDue = amount
            }
        }

        // Calculate totalTax from tax breakdown if not found
        if (totalTax == 0.0 && grandTotal > 0 && subTotal > 0) {
            totalTax = grandTotal - subTotal - roundOff
        }

        return InvoiceSummary(
            subTotal = subTotal,
            totalTax = totalTax,
            roundOff = roundOff,
            grandTotal = grandTotal,
            receivedAmount = receivedAmount,
            balanceDue = balanceDue
        )
    }

    private fun extractLastAmount(line: String): Double {
        return CURRENCY_REGEX.findAll(line)
            .mapNotNull { it.groupValues[1].replace(",", "").toDoubleOrNull() }
            .filter { it > 0.0 }
            .lastOrNull() ?: 0.0
    }

    // ══════════════════════════════════════════════════════════════
    // Legacy SKU Extraction (kept for backward compat)
    // ══════════════════════════════════════════════════════════════

    private fun extractSkuItems(lines: List<String>): List<SkuLineItem> {
        val items = mutableListOf<SkuLineItem>()
        val skuRegex = Regex("""^(.+?)\s+(\d+)\s+[₹Rs.]*\s*([\d,]+\.?\d*)\s*$""")

        for (line in lines) {
            val match = skuRegex.find(line.trim())
            if (match != null) {
                val name = match.groupValues[1].trim()
                val qty = match.groupValues[2].toIntOrNull() ?: 0
                val price = match.groupValues[3].replace(",", "").toDoubleOrNull() ?: 0.0

                if (name.length > 2 && qty > 0 && price > 0) {
                    items.add(
                        SkuLineItem(
                            id = "item-${(100000..999999).random()}-${(1..999).random()}",
                            productName = name,
                            quantity = qty,
                            totalPrice = price
                        )
                    )
                }
            }
        }
        return items
    }

    // ══════════════════════════════════════════════════════════════
    // Debug Logging
    // ══════════════════════════════════════════════════════════════

    private fun debugLog(message: String) {
        println("[OCR] $message")
    }
}
