package com.siteflow.retailsync.core.presentation.util

/**
 * KMP-compatible number formatting (String.format is JVM-only).
 */
fun Double.formatCurrency(decimals: Int = 2): String {
    val factor = pow10(decimals)
    val rounded = kotlin.math.round(this * factor) / factor
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val decPart = if (decimals > 0) {
        val raw = parts.getOrElse(1) { "" }
        raw.padEnd(decimals, '0').take(decimals)
    } else ""
    return if (decimals > 0) "$intPart.$decPart" else intPart
}

private fun pow10(n: Int): Double {
    var result = 1.0
    repeat(n) { result *= 10.0 }
    return result
}
