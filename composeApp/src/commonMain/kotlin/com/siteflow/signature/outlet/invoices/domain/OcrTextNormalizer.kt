package com.siteflow.signature.outlet.invoices.domain

/**
 * Pre-processes raw OCR lines to stabilize parsing results.
 *
 * Fixes common OCR inconsistencies:
 * - Varying whitespace
 * - Short line fragments that should be merged
 * - Duplicate lines from double-detection
 * - Empty/garbage lines
 */
object OcrTextNormalizer {

    /**
     * Normalize OCR lines for consistent parsing.
     * @param lines Raw lines from OcrResult
     * @return Cleaned, stabilized list of lines
     */
    fun normalize(lines: List<String>): List<String> {
        if (lines.isEmpty()) return lines

        return lines
            .map { normalizeWhitespace(it) }   // 1. Clean individual lines
            .filter { it.isNotBlank() }         // 2. Remove empty lines
            .let { mergeShortFragments(it) }    // 3. Merge broken lines
            .let { deduplicateLines(it) }       // 4. Remove duplicate lines
            .filter { it.length >= 2 }          // 5. Remove single-char garbage
    }

    /**
     * Collapse multiple spaces, tabs, and trim.
     */
    private fun normalizeWhitespace(line: String): String {
        return line
            .replace('\t', ' ')
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
    }

    /**
     * Merge short line fragments (< 5 chars) into the previous or next line.
     *
     * OCR engines sometimes split a single line into multiple fragments like:
     *   "Invoice No"
     *   ":"
     *   "33989"
     * This merges them back into: "Invoice No : 33989"
     */
    private fun mergeShortFragments(lines: List<String>): List<String> {
        if (lines.size <= 1) return lines

        val result = mutableListOf<String>()
        var i = 0

        while (i < lines.size) {
            val current = lines[i]

            // If current line is very short (just a separator or fragment)
            if (current.length < 5 && !isStandaloneValue(current)) {
                // Merge with previous line if available
                if (result.isNotEmpty()) {
                    result[result.lastIndex] = result.last() + " " + current
                }
                // Also check if next line should be appended
                if (i + 1 < lines.size && lines[i + 1].length < 10 && isLikelyValueFragment(lines[i + 1])) {
                    result[result.lastIndex] = result.last() + " " + lines[i + 1]
                    i += 2
                    continue
                }
            } else {
                result.add(current)
            }
            i++
        }

        return result
    }

    /**
     * Remove ADJACENT duplicate lines (OCR sometimes detects the same text twice in a row).
     * Preserves order and keeps non-adjacent duplicates (e.g., same product name
     * appearing for 200ml and 500ml variants).
     */
    private fun deduplicateLines(lines: List<String>): List<String> {
        if (lines.size <= 1) return lines
        val result = mutableListOf(lines.first())
        for (i in 1 until lines.size) {
            val current = lines[i].lowercase().trim()
            val previous = lines[i - 1].lowercase().trim()
            if (current != previous) {
                result.add(lines[i])
            }
        }
        return result
    }

    /**
     * Check if a short string is a standalone value (number, date) that shouldn't be merged.
     */
    private fun isStandaloneValue(s: String): Boolean {
        val trimmed = s.trim()
        // Pure numbers like "33989", dates like "15-Sep-25"
        if (trimmed.all { it.isDigit() } && trimmed.length >= 3) return true
        if (Regex("""\d{1,2}[/\-]\w+[/\-]\d{2,4}""").containsMatchIn(trimmed)) return true
        return false
    }

    /**
     * Check if a string looks like a value that belongs on the previous line.
     */
    private fun isLikelyValueFragment(s: String): Boolean {
        val trimmed = s.trim()
        // Numbers, dates, short codes
        if (trimmed.all { it.isDigit() || it == '-' || it == '/' }) return true
        return false
    }
}
