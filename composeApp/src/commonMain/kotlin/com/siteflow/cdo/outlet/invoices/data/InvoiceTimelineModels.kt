package com.siteflow.cdo.outlet.invoices.data

import com.siteflow.cdo.ase.invoices.data.InvoiceItem
import com.siteflow.cdo.ase.invoices.data.InvoiceStatus
/**
 * Status of a timeline step.
 */
enum class TimelineStepStatus {
    COMPLETED,
    PROCESSING,
    PENDING
}

/**
 * A single step in the invoice status timeline.
 */
data class InvoiceTimelineStep(
    val title: String,
    val date: String = "",
    val subtitle: String = "",
    val status: TimelineStepStatus = TimelineStepStatus.PENDING,
    val statusLabel: String = "",
    /** Extra detail fields shown as key-value pairs inside the card */
    val details: List<Pair<String, String>> = emptyList(),
    /** Optional progress bar (0f..1f) */
    val progressValue: Float? = null,
    val progressLabel: String? = null,
    /** Optional achievement badge text */
    val badge: String? = null,
    val description: String? = null,
    /** Rejection reason (for failed step) */
    val rejectionReason: String? = null,
    /** Whether to wrap this step in an elevated card */
    val isHighlighted: Boolean = false
)

/**
 * Summary info displayed at the top of the timeline.
 */
data class InvoiceTimelineSummary(
    val invoiceNumber: String,
    val amount: String,
    val distributorName: String,
    val submissionDate: String,
    val cyclePeriod: String,
    val isPayoutEligible: Boolean = false,
    val estimatedPayoutAmount: String? = null
)

/**
 * Mock timeline — 5-step lifecycle:
 * 1. Uploaded by Outlet
 * 2. Approved by ASE
 * 3. Approved by ASM
 * 4. Approved by Consumer Finance
 * 5. Payout
 */
fun mockInvoiceTimeline(): List<InvoiceTimelineStep> = listOf(
    // ── Step 1: Uploaded ──
    InvoiceTimelineStep(
        title = "Invoice Uploaded",
        date = "15 Feb",
        subtitle = "Uploaded by Outlet Admin",
        status = TimelineStepStatus.COMPLETED,
        statusLabel = "SUCCESS"
    ),
    // ── Step 2: ASE Approval ──
    InvoiceTimelineStep(
        title = "ASE Approval",
        date = "16 Feb",
        subtitle = "Approved by Rahul Sharma (ASE)",
        status = TimelineStepStatus.COMPLETED,
        statusLabel = "Approved",
        details = listOf(
            "VARIANCE" to "+12%",
            "STATUS" to "Approved"
        )
    ),
    // ── Step 3: ASM Approval ──
    InvoiceTimelineStep(
        title = "ASM Approval",
        date = "17 Feb",
        subtitle = "Approved by Vijay Kumar (ASM)",
        status = TimelineStepStatus.COMPLETED,
        statusLabel = "Approved"
    ),
    // ── Step 4: Consumer Finance ──
    InvoiceTimelineStep(
        title = "Consumer Finance",
        date = "Started 18 Feb",
        status = TimelineStepStatus.PROCESSING,
        statusLabel = "Processing",
        description = "Finance team is verifying invoice and calculating eligible payout.",
        details = listOf(
            "INVOICE" to "₹12,450",
            "ELIGIBLE" to "₹2,900"
        ),
        isHighlighted = true
    ),
    // ── Step 5: Payout ──
    InvoiceTimelineStep(
        title = "Payout",
        date = "Est. 15 Mar",
        subtitle = "Scheduled for next payment cycle.",
        status = TimelineStepStatus.PENDING,
        details = listOf(
            "EST. AMOUNT" to "₹2,900",
            "VIA" to "Bank Transfer"
        ),
        description = "Payout in ~17 days"
    )
)

fun mockInvoiceTimelineSummary(): InvoiceTimelineSummary = InvoiceTimelineSummary(
    invoiceNumber = "INV-2026-0047",
    amount = "₹12,450",
    distributorName = "ABC Distributors Pvt Ltd",
    submissionDate = "15 Feb 2026",
    cyclePeriod = "February 2026 Cycle",
    isPayoutEligible = true,
    estimatedPayoutAmount = "₹2,900"
)

// ═══════════════════════════════════════════════════════════
// Real data builders — driven by InvoiceItem.status
// ═══════════════════════════════════════════════════════════

/**
 * Builds timeline summary from a real [InvoiceItem].
 */
fun buildSummaryFromInvoice(invoice: InvoiceItem): InvoiceTimelineSummary {
    return InvoiceTimelineSummary(
        invoiceNumber = invoice.invoiceNumber.ifBlank { invoice.id },
        amount = invoice.invoiceAmount,
        distributorName = invoice.distributorName.ifBlank { invoice.location },
        submissionDate = invoice.invoicePeriod,
        cyclePeriod = "Invoice Period: ${invoice.invoicePeriod}",
        isPayoutEligible = invoice.status == InvoiceStatus.PAID || invoice.status == InvoiceStatus.FINANCE_APPROVED
    )
}

/**
 * Builds a 5-step timeline from the invoice status.
 *
 * Steps:
 * 1. Invoice Submitted
 * 2. ASE Review
 * 3. ASM Review
 * 4. Finance Verification
 * 5. Payout
 *
 * Status progression order:
 * SUBMITTED → ASE_APPROVED → ASM_APPROVED → FINANCE_APPROVED → CALCULATED → PAID
 * REJECTED can happen at any stage.
 */
fun buildTimelineFromStatus(invoice: InvoiceItem): List<InvoiceTimelineStep> {
    val status = invoice.status
    val isRejected = status == InvoiceStatus.REJECTED

    // Determine which stage the rejection happened at (best guess from status)
    // Since API doesn't tell us *which* level rejected, we show it at the current step
    val statusOrder = listOf(
        InvoiceStatus.SUBMITTED,
        InvoiceStatus.ASE_APPROVED,
        InvoiceStatus.ASM_APPROVED,
        InvoiceStatus.FINANCE_APPROVED,
        InvoiceStatus.CALCULATED,
        InvoiceStatus.PAID
    )
    val currentIndex = statusOrder.indexOf(status)

    fun stepStatus(requiredIndex: Int): TimelineStepStatus {
        if (isRejected) {
            // For rejected invoices, step 1 (submitted) is always complete
            return if (requiredIndex == 0) TimelineStepStatus.COMPLETED
            else TimelineStepStatus.PENDING
        }
        return when {
            currentIndex >= requiredIndex -> TimelineStepStatus.COMPLETED
            currentIndex == requiredIndex - 1 -> TimelineStepStatus.PROCESSING
            else -> TimelineStepStatus.PENDING
        }
    }

    return listOf(
        // ── Step 1: Submitted ──
        InvoiceTimelineStep(
            title = "Invoice Submitted",
            date = invoice.submittedTime,
            subtitle = "Your invoice has been received",
            status = if (isRejected || currentIndex >= 0) TimelineStepStatus.COMPLETED else TimelineStepStatus.PENDING,
            statusLabel = "Received"
        ),

        // ── Step 2: ASE Review ──
        InvoiceTimelineStep(
            title = "ASE Review",
            subtitle = when {
                isRejected -> "Review could not be completed"
                stepStatus(1) == TimelineStepStatus.COMPLETED -> "Approved by your Sales Executive"
                stepStatus(1) == TimelineStepStatus.PROCESSING -> "Awaiting Sales Executive approval"
                else -> "Pending review by Sales Executive"
            },
            status = stepStatus(1),
            statusLabel = when (stepStatus(1)) {
                TimelineStepStatus.COMPLETED -> "Approved"
                TimelineStepStatus.PROCESSING -> "Awaiting Approval"
                TimelineStepStatus.PENDING -> ""
            },
            isHighlighted = stepStatus(1) == TimelineStepStatus.PROCESSING,
            rejectionReason = if (isRejected && currentIndex < 1) invoice.reviewNote else null
        ),

        // ── Step 3: ASM Review ──
        InvoiceTimelineStep(
            title = "ASM Review",
            subtitle = when {
                isRejected -> "Review could not be completed"
                stepStatus(2) == TimelineStepStatus.COMPLETED -> "Approved by Area Sales Manager"
                stepStatus(2) == TimelineStepStatus.PROCESSING -> "Awaiting ASM approval"
                else -> "Pending review by Area Sales Manager"
            },
            status = stepStatus(2),
            statusLabel = when (stepStatus(2)) {
                TimelineStepStatus.COMPLETED -> "Approved"
                TimelineStepStatus.PROCESSING -> "Awaiting Approval"
                TimelineStepStatus.PENDING -> ""
            },
            isHighlighted = stepStatus(2) == TimelineStepStatus.PROCESSING,
            rejectionReason = if (isRejected && currentIndex in 1..2) invoice.reviewNote else null
        ),

        // ── Step 4: Finance Verification ──
        InvoiceTimelineStep(
            title = "Finance Verification",
            subtitle = when {
                isRejected -> "Verification could not be completed"
                currentIndex >= 4 -> "Amount verified and calculated"  // CALCULATED or PAID
                stepStatus(3) == TimelineStepStatus.COMPLETED -> "Finance team has verified"
                stepStatus(3) == TimelineStepStatus.PROCESSING -> "Being verified by finance team"
                else -> "Pending finance verification"
            },
            status = if (currentIndex >= 4) TimelineStepStatus.COMPLETED else stepStatus(3),
            statusLabel = when {
                currentIndex >= 4 -> "Verified"
                stepStatus(3) == TimelineStepStatus.COMPLETED -> "Verified"
                stepStatus(3) == TimelineStepStatus.PROCESSING -> "Processing"
                else -> ""
            },
            isHighlighted = stepStatus(3) == TimelineStepStatus.PROCESSING || status == InvoiceStatus.CALCULATED,
            description = if (status == InvoiceStatus.CALCULATED) "Your invoice amount is being calculated for payout." else null,
            rejectionReason = if (isRejected && currentIndex >= 3) invoice.reviewNote else null
        ),

        // ── Step 5: Payout ──
        InvoiceTimelineStep(
            title = "Payout",
            subtitle = when {
                status == InvoiceStatus.PAID -> "Payment has been processed"
                isRejected -> "Payout cancelled"
                else -> "Scheduled for next payment cycle"
            },
            status = when {
                status == InvoiceStatus.PAID -> TimelineStepStatus.COMPLETED
                status == InvoiceStatus.CALCULATED || status == InvoiceStatus.FINANCE_APPROVED -> TimelineStepStatus.PROCESSING
                else -> TimelineStepStatus.PENDING
            },
            statusLabel = when {
                status == InvoiceStatus.PAID -> "Paid"
                status == InvoiceStatus.CALCULATED || status == InvoiceStatus.FINANCE_APPROVED -> "Upcoming"
                else -> ""
            },
            isHighlighted = status == InvoiceStatus.PAID,
            details = emptyList(),
            badge = if (status == InvoiceStatus.PAID) "Payment Complete ✓" else null
        )
    )
}
