package com.siteflow.signature.core.domain

enum class InvoiceFilter(val label: String) {
    /** Full colour, no grayscale or contrast change. */
    ORIGINAL("Original"),
    /** Grayscale + moderate contrast boost — best for most invoices. */
    GRAYSCALE("Grayscale"),
    /** Grayscale + high contrast — for faded / low-ink invoices. */
    HIGH_CONTRAST("Hi-Contrast")
}
