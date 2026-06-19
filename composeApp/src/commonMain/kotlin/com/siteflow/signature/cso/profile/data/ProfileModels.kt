package com.siteflow.signature.cso.profile.data

import androidx.compose.ui.graphics.Color
import com.siteflow.signature.core.presentation.design.AppColors

/**
 * KPI trend direction.
 */
enum class KpiTrend { UP, DOWN, FLAT }

/**
 * Icon type for KPI cards — maps to Material Icons in the UI layer.
 */
enum class KpiIcon { OUTLETS, PFP, BILLING, INCENTIVE }

/**
 * A single KPI metric for the performance snapshot.
 */
data class KpiCard(
    val label: String,
    val value: String,
    val trendPercent: Int,
    val trend: KpiTrend,
    val icon: KpiIcon,
    val iconBgColor: Color,
    val iconTintColor: Color
)

/**
 * ASE profile data.
 */
data class AseProfile(
    val name: String,
    val role: String,
    val department: String,
    val employeeId: String,
    val location: String,
    val initials: String,
    val kpis: List<KpiCard>,
    val outletProfile: OutletProfile? = null
)

/**
 * Specialized profile data for Outlet role.
 */
data class OutletProfile(
    val tier: String,
    val status: String,
    val ownerName: String,
    val profileImageUrl: String? = null,
    val performance: OutletPerformance,
    val bankDetails: BankDetails,
    val documents: List<DocumentEntry>
)

data class OutletPerformance(
    val forecastTarget: Double,
    val invoiceSubmitted: Double,
    val variance: Int,
    val isEligible: Boolean,
    val expectedPayout: Double,
    val lastUpdated: String
)

data class BankDetails(
    val accountHolder: String,
    val bankName: String,
    val ifscCode: String,
    val maskedAccountNumber: String,
    val isVerified: Boolean,
    val accountType: String? = null
)

data class DocumentEntry(
    val name: String,
    val status: String
)

/**
 * Mock profile for development.
 */
val mockAseProfile = AseProfile(
    name = "Rahul Sharma",
    role = "Sales Executive",
    department = "CSD",
    employeeId = "Emp-4092",
    location = "Bangalore East",
    initials = "RS",
    kpis = listOf(
        KpiCard(
            label = "Outlets Acquired",
            value = "24",
            trendPercent = 12,
            trend = KpiTrend.UP,
            icon = KpiIcon.OUTLETS,
            iconBgColor = Color(0xFFCCFBF1),
            iconTintColor = AppColors.BlueGradientStart
        ),
        KpiCard(
            label = "Active PFP Outlets",
            value = "18",
            trendPercent = 5,
            trend = KpiTrend.DOWN,
            icon = KpiIcon.PFP,
            iconBgColor = Color(0xFFE0E7FF),
            iconTintColor = Color(0xFF6366F1)
        ),
        KpiCard(
            label = "Monthly Billing",
            value = "₹3.2L",
            trendPercent = 2,
            trend = KpiTrend.DOWN,
            icon = KpiIcon.BILLING,
            iconBgColor = Color(0xFFD1FAE5),
            iconTintColor = AppColors.Success
        ),
        KpiCard(
            label = "Incentives Earned",
            value = "₹12k",
            trendPercent = 8,
            trend = KpiTrend.UP,
            icon = KpiIcon.INCENTIVE,
            iconBgColor = Color(0xFFFEF3C7),
            iconTintColor = Color(0xFFF59E0B)
        )
    )
)

/**
 * Mock Profile for ASM development.
 */
val mockAsmProfile = AseProfile(
    name = "Vikram Singh",
    role = "Area Sales Manager",
    department = "CSD",
    employeeId = "Emp-A012",
    location = "Bangalore Region",
    initials = "VS",
    kpis = listOf(
        KpiCard(
            label = "Team Outlets Acquired",
            value = "142",
            trendPercent = 22,
            trend = KpiTrend.UP,
            icon = KpiIcon.OUTLETS,
            iconBgColor = Color(0xFFCCFBF1),
            iconTintColor = AppColors.BlueGradientStart
        ),
        KpiCard(
            label = "Team Active PFPs",
            value = "86",
            trendPercent = 12,
            trend = KpiTrend.UP,
            icon = KpiIcon.PFP,
            iconBgColor = Color(0xFFE0E7FF),
            iconTintColor = Color(0xFF6366F1)
        ),
        KpiCard(
            label = "Region Monthly Billing",
            value = "₹18.5L",
            trendPercent = 5,
            trend = KpiTrend.UP,
            icon = KpiIcon.BILLING,
            iconBgColor = Color(0xFFD1FAE5),
            iconTintColor = AppColors.Success
        ),
        KpiCard(
            label = "Pending L2 Approvals",
            value = "14",
            trendPercent = 2,
            trend = KpiTrend.DOWN,
            icon = KpiIcon.INCENTIVE, // Can repurpose for now
            iconBgColor = Color(0xFFFEE2E2),
            iconTintColor = AppColors.Danger
        )
    )
)

/**
 * Mock Profile for Outlet development.
 */
val mockOutletProfile = AseProfile(
    name = "Green Valley Mart",
    role = "Retail Outlet",
    department = "PFP Partner",
    employeeId = "OUT-1042",
    location = "Mumbai Central, West Zone",
    initials = "GV",
    kpis = emptyList(),
    outletProfile = OutletProfile(
        tier = "PLATINUM",
        status = "ACTIVE",
        ownerName = "Rajesh Kumar",
        profileImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200&auto=format&fit=crop",
        performance = OutletPerformance(
            forecastTarget = 50000.0,
            invoiceSubmitted = 42000.0,
            variance = -16,
            isEligible = true,
            expectedPayout = 2900.0,
            lastUpdated = "Updated today"
        ),
        bankDetails = BankDetails(
            accountHolder = "Rajesh Kumar",
            bankName = "HDFC Bank",
            ifscCode = "HDFC0001234",
            maskedAccountNumber = "•••• •••• •••• 8921",
            isVerified = true
        ),
        documents = listOf(
            DocumentEntry("PFP Agreement", "Signed")
        )
    )
)
