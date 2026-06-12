package com.siteflow.signature.core.util

/**
 * Converts a string to Title Case.
 *
 * Examples:
 *   "dabur store"        → "Dabur Store"
 *   "KRISHNA GENERAL"    → "Krishna General"
 *   "john's shop"        → "John's Shop"
 *   "   multiple  spaces " → "Multiple Spaces"
 *
 * Words ≤ 2 characters that are common articles/prepositions are kept
 * lowercase (unless they are the first word). This gives a more natural,
 * professional look: "The King of Store" instead of "The King Of Store".
 */
fun String.toTitleCase(): String {
    if (isBlank()) return this

    val minorWords = setOf("of", "the", "and", "in", "on", "at", "to", "for", "a", "an", "by")

    return trim()
        .split("\\s+".toRegex())
        .mapIndexed { index, word ->
            if (index > 0 && word.lowercase() in minorWords) {
                word.lowercase()
            } else {
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        }
        .joinToString(" ")
}
