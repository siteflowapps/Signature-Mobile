package com.siteflow.cdo.core.domain

/** Returns current date-time as "dd/MM/yyyy HH:mm" — platform-implemented. */
expect fun nowLabel(): String

/** Truncates a Double to 4 decimal places (KMP-safe, no String.format). */
fun truncate4(d: Double): String {
    val whole = d.toLong()
    val frac = ((d - whole) * 10000).toLong().let { if (d < 0) -it else it }
    return "$whole.${frac.toString().padStart(4, '0')}"
}
