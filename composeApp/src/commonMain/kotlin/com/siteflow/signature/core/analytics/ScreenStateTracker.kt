package com.siteflow.signature.core.analytics

import com.siteflow.signature.core.navigation.AppDestination

/**
 * Maps navigation route strings to human-readable screen names for analytics.
 * Add new destinations here whenever a new screen is added to AppDestination.
 */
object ScreenStateTracker {

    fun screenName(route: String?): String = when {
        route == null -> "unknown"
        // Auth
        route == AppDestination.Login.route -> "Login"
        route == AppDestination.VerifyOtp.route -> "OTP Verification"
        route == AppDestination.RoleSelect.route -> "Role Select"
        // ASE
        route == AppDestination.CsoHome.route -> "ASE Home"
        route == AppDestination.CsoDashboard.route -> "ASE Outlets"
        route == AppDestination.CsoOnboardOutlet.route -> "Onboarding Step 1"
        route == AppDestination.CsoOnboardStep2.route -> "Onboarding Step 2"
        route == AppDestination.CsoOnboardStep3.route -> "Onboarding Step 3"
        route == AppDestination.CsoOnboardStep4.route -> "Onboarding Step 4"
        route == AppDestination.CsoOnboardStep5.route -> "Onboarding Step 5"
        route == AppDestination.CsoOnboardStep6.route -> "Onboarding Step 6"
        route == AppDestination.CsoOutletDetails.route -> "Outlet Details"
        route == AppDestination.CsoCompliance.route -> "Compliance"
        route == AppDestination.CsoInvoices.route -> "ASE Invoices"
        route == AppDestination.CsoInvoiceDetail.route -> "ASE Invoice Detail"
        // Outlet
        route == AppDestination.OutletDashboard.route -> "Outlet Dashboard"
        route == AppDestination.OutletInvoices.route -> "Outlet Invoices"
        route == AppDestination.OutletInvoiceDetail.route -> "Outlet Invoice Detail"
        route == AppDestination.OutletUploadInvoice.route -> "Upload Invoice"
        route == AppDestination.OutletInvoiceCropReview.route -> "Invoice Crop Review"
        route == AppDestination.OutletInvoiceProcessing.route -> "Invoice AI Processing"
        route == AppDestination.OutletInvoiceReview.route -> "Invoice Review"
        route.startsWith("outlet/region-scan") -> "Invoice Region Scan"
        // Walkthrough
        route == AppDestination.WalkthroughWelcome.route -> "Walkthrough Welcome"
        route == AppDestination.WalkthroughOutletDetails.route -> "Walkthrough Outlet Details"
        route == AppDestination.WalkthroughAgreement.route -> "Walkthrough Agreement"
        route == AppDestination.WalkthroughPayment.route -> "Walkthrough Payment"
        // ASM
        route == AppDestination.AsmHome.route -> "ASM Home"
        route == AppDestination.AsmDashboard.route -> "ASM Outlets"
        route == AppDestination.AsmOutletReview.route -> "ASM Outlet Review"
        route == AppDestination.AsmInvoices.route -> "ASM Invoices"
        route == AppDestination.AsmInvoiceDetail.route -> "ASM Invoice Detail"
        route == AppDestination.AsmMyTeam.route -> "My Team"
        // Shared
        route == AppDestination.Profile.route -> "Profile"
        else -> route
    }
}
