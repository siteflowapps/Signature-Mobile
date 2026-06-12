 package com.siteflow.signature.outlet.dashboard.data

import androidx.compose.ui.graphics.Color
import com.siteflow.signature.cso.invoices.data.InvoiceStatus
import com.siteflow.signature.core.presentation.design.AppColors

// ═══════════════════════════════════════════════════════════
// Outlet Tier
// ═══════════════════════════════════════════════════════════

enum class OutletTier(
    val label: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color
) {
    BRONZE("Bronze", "🥉", Color(0xFFCD7F32), Color(0xFFFDF2E6)),
    SILVER("Silver", "🥈", Color(0xFF9CA3AF), Color(0xFFF3F4F6)),
    GOLD("Gold", "🏆", Color(0xFFF59E0B), Color(0xFFFEF3C7))
}

// ═══════════════════════════════════════════════════════════
// Payout Summary (redesigned — no next payout)
// ═══════════════════════════════════════════════════════════

data class OutletPayoutSummary(
    val lastPayoutAmount: String = "₹0.00",
    val lastPayoutDate: String? = null,
    val totalInvoices: Int = 0,
    val pendingInvoices: Int = 0
)

// ═══════════════════════════════════════════════════════════
// Notification
// ═══════════════════════════════════════════════════════════

enum class NotificationType { INFO, WARNING, ERROR, SUCCESS }

data class OutletNotification(
    val id: String,
    val message: String,
    val type: NotificationType,
    val invoiceId: String? = null,
    val actionLabel: String? = null
)

// ═══════════════════════════════════════════════════════════
// Monthly Performance
// ═══════════════════════════════════════════════════════════

data class OutletMonthlyPerformance(
    val month: String,
    val forecastTarget: String,
    val approvedAmount: String,
    val hasPendingInvoice: Boolean,
    // ── Invoice Breakdown ──
    val totalInvoices: Int = 0,
    val approvedCount: Int = 0,
    val pendingCount: Int = 0,
    val rejectedCount: Int = 0,
    // ── Month-over-Month Trend ──
    val vsLastMonth: String? = null,
    val vsLastMonthPositive: Boolean = true,
)

// ═══════════════════════════════════════════════════════════
// Recent Invoice (with status)
// ═══════════════════════════════════════════════════════════

data class OutletRecentInvoice(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val accentColor: Color = AppColors.BlueGradientStart
)
