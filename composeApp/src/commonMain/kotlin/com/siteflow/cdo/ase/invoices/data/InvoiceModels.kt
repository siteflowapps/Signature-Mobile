package com.siteflow.cdo.ase.invoices.data

import androidx.compose.ui.graphics.Color
import com.siteflow.cdo.ase.dashboard.data.OutletSlab
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.shared.data.ApprovalStep

/**
 * Review status of an invoice.
 */
enum class InvoiceStatus(
    val label: String,
    val color: Color,
    val bgColor: Color
) {
    PENDING("Pending", Color(0xFFF59E0B), Color(0xFFFEF3C7)), // Legacy/Fallback
    SUBMITTED("Submitted", Color(0xFF6B7280), Color(0xFFF3F4F6)),
    ASE_APPROVED("ASE Approved", Color(0xFF3B82F6), Color(0xFFDBEAFE)),
    ASM_APPROVED("ASM Approved", Color(0xFF8B5CF6), Color(0xFFEDE9FE)),
    FINANCE_APPROVED("Finance Approved", Color(0xFF10B981), Color(0xFFD1FAE5)),
    CALCULATED("Calculated", Color(0xFFEC4899), Color(0xFFFCE7F3)),
    PAID("Paid", Color(0xFF059669), Color(0xFFD1FAE5)),
    APPROVED("Approved", AppColors.Success, Color(0xFFD1FAE5)), // Legacy
    REJECTED("Rejected", AppColors.Danger, Color(0xFFFEE2E2))
}

/**
 * Whether the invoice amount meets the slab threshold.
 */
enum class SlabQualification(val label: String, val color: Color) {
    MEETS("Meets threshold", AppColors.Success),
    BELOW("Below threshold", Color(0xFFF59E0B))
}

/**
 * A single product line item on an invoice, matching the API items array.
 */
data class InvoiceLineItem(
    val skuName: String,
    val quantity: String,   // e.g. "12 No."
    val unit: String,       // e.g. "No."
    val unitPrice: Double,
    val totalPrice: Double,

    // ── Rich fields from AI extraction / catalog matching ──
    val matchedSkuName: String? = null,
    val invoicedSkuName: String = "",
    val caseConfiguration: Int? = null,
    val mrpPerCase: Double? = null,
    val mrpPerBottle: Double? = null,
    val finalQuantity: Int = 0,
    val finalUnit: String = ""
)

/**
 * Single invoice entry for the ASE Invoice List.
 */
data class InvoiceItem(
    val id: String,
    val outletName: String,
    val initials: String,
    val slab: OutletSlab,
    val location: String,
    val status: InvoiceStatus,
    val invoiceAmount: String,
    val invoicePeriod: String,
    val lineItemCount: Int,
    val slabQualification: SlabQualification,
    val submittedTime: String,
    val lineItems: List<InvoiceLineItem> = emptyList(),
    val invoiceImageUrl: String? = null,
    val reviewNote: String? = null,

    // ── Invoice metadata ──
    val invoiceNumber: String = "",
    val distributorName: String = "",
    val totalQuantity: Double = 0.0,

    // ── Multi-level approval ──
    val submittedByAse: String = "",
    val approvalTimeline: List<ApprovalStep> = emptyList()
) {
    /**
     * Total cases in this invoice.
     * The API guarantees all finalUnit values are already "CASE", so this is
     * a direct sum of finalQuantity across all matched line items.
     */
    val totalCases: Int
        get() = lineItems.sumOf { it.finalQuantity }
}

