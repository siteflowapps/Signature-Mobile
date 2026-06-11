package com.siteflow.cdo.shared.data

import kotlinx.serialization.Serializable

/**
 * Response wrapper for GET /payouts/calculate/{invoiceId}.
 */
@Serializable
data class PayoutCalculationResponseDto(
    val success: Boolean,
    val data: PayoutCalculationDto? = null,
    val timestamp: String? = null
)

/**
 * A single SKU line item within the payout calculation.
 */
@Serializable
data class PayoutLineItemDto(
    val skuName: String? = null,
    val category: String? = null,
    val mrpPerCase: Double? = null,
    val invoicedQuantity: Int? = null,
    val caseConfiguration: Int? = null,
    val physicalCases: Double? = null,
    val mrpRevenue: Double? = null,
    val ratePerCase: Double? = null,
    val payoutAmount: Double? = null,
    val excludedFromPayout: Boolean? = null
)

/**
 * Payout calculation result for an invoice.
 *
 * New model: payout = totalCases × ratePerCase (fixed ₹ per case per slab tier).
 * slabPercentage is kept nullable for backward compat but is no longer
 * the primary driver.
 */
@Serializable
data class PayoutCalculationDto(
    val outletId: String? = null,
    val outletName: String? = null,
    val complianceState: String? = null,
    val slabId: String? = null,
    val classification: String? = null,
    val minQuantity: Double? = null,
    val maxQuantity: Double? = null,
    /** Legacy — kept for backward compat, no longer the primary payout driver. */
    val slabPercentage: Double? = null,
    val invoiceTotalAmount: Double? = null,
    val calculatedPayoutAmount: Double? = null,
    /** Fixed ₹ rate per case for the slab this outlet qualifies for. */
    val ratePerCase: Double? = null,
    /** Total case count in the invoice (API converts all units to CASE). */
    val totalCases: Int? = null,

    // ── New fields from updated API ──────────────────────────────
    /** Cumulative monthly volume across ALL invoices this month (in physical cases). */
    val totalMonthlyVolumePc: Double? = null,
    /** Volume that qualifies for the current slab (may exclude water). */
    val slabVolumePc: Double? = null,
    /** Water-category volume (tracked separately). */
    val waterVolumePc: Double? = null,
    /** True when the payout is an estimate (not yet finalised by finance). */
    val isEstimated: Boolean? = null,
    /** True when the outlet has not yet crossed the minimum slab threshold. */
    val belowMinimumThreshold: Boolean? = null,
    /** Total MRP revenue for this invoice. */
    val invoiceTotalMrpRevenue: Double? = null,
    /** Per-SKU line items with individual payout breakdown. */
    val lineItems: List<PayoutLineItemDto> = emptyList()
) {
    /**
     * Resolved ₹/case rate: use server value if present, otherwise derive from
     * the slab tier name so the UI can still display something meaningful
     * while the backend is being updated.
     *
     * Tier  | Cases     | ₹/case
     * SILVER | 40–80    | 3.0
     * GOLD   | 81–149   | 4.0
     * DIAMOND| 150–299  | 6.0
     * PLATINUM| 300+    | 8.0
     */
    val resolvedRatePerCase: Double?
        get() = ratePerCase ?: when (classification?.uppercase()) {
            "SILVER" -> 3.0
            "GOLD"   -> 4.0
            "DIAMOND" -> 6.0
            "PLATINUM" -> 8.0
            else -> null
        }

    /** "Silver", "Gold", "Diamond", "Platinum" (or raw classification). */
    val displayClassification: String
        get() = when (classification?.uppercase()) {
            "SILVER"   -> "Silver"
            "GOLD"     -> "Gold"
            "DIAMOND"  -> "Diamond"
            "PLATINUM" -> "Platinum"
            else -> classification
                ?.replace("_CLASS", "")
                ?.replace("_", " ")
                ?.lowercase()
                ?.replaceFirstChar { it.uppercase() }
                ?: "—"
        }

    /** Tier emoji prefix for badge display. */
    val tierEmoji: String
        get() = when (classification?.uppercase()) {
            "SILVER"   -> "🥈"
            "GOLD"     -> "🥇"
            "DIAMOND"  -> "💎"
            "PLATINUM" -> "🏆"
            else -> ""
        }

    /** "40 – 80 cases" or "300+ cases". */
    val slabRange: String
        get() {
            val min = minQuantity?.toInt() ?: return "—"
            val max = maxQuantity?.toInt() ?: return "$min+ cases"
            return "$min – $max cases"
        }

    /**
     * Whether the outlet has enough cases to qualify for any slab.
     * Minimum threshold is 40 cases (Silver floor).
     */
    val isEligible: Boolean
        get() = belowMinimumThreshold == false || (totalMonthlyVolumePc ?: totalCases?.toDouble() ?: 0.0) >= 40.0

    /**
     * Human-readable formula: "31 cases × ₹3.00 = ₹93.00"
     * Used as the subtitle on the payout card.
     */
    val payoutBreakdown: String
        get() {
            val cases = totalMonthlyVolumePc?.toInt() ?: totalCases ?: return ""
            val rate = resolvedRatePerCase ?: return ""
            val payout = monthlyEstimatedPayout ?: calculatedPayoutAmount ?: (cases * rate)
            return "$cases cases × ₹${formatRate(rate)} = ₹${formatRate(payout)}"
        }

    /**
     * Total monthly payout = cumulative monthly cases × fixed rate per case.
     * This is the correct "Monthly Payout Estimate" figure — NOT [calculatedPayoutAmount]
     * which only reflects this single invoice's contribution.
     */
    val monthlyEstimatedPayout: Double?
        get() = totalMonthlyVolumePc?.let { it * (resolvedRatePerCase ?: 0.0) }
}

private fun formatRate(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        val whole = value.toLong()
        val frac = ((value - whole) * 100).toLong()
        "$whole.${frac.toString().padStart(2, '0')}"
    }
}
