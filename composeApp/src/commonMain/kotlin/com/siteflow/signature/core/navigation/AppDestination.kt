package com.siteflow.signature.core.navigation

sealed class AppDestination(val route: String) {

    // Auth (shared)
    object Login : AppDestination("login")
    object VerifyOtp : AppDestination("verify-otp")
    object RoleSelect : AppDestination("role-select")

    // CSO (Customer Sales Officer) — field onboarding
    object CsoHome : AppDestination("cso/home")
    object CsoDashboard : AppDestination("cso/dashboard")
    object CsoOnboardOutlet : AppDestination("cso/onboard")
    object CsoOnboardStep2 : AppDestination("cso/onboard/step2")
    object CsoOnboardStep3 : AppDestination("cso/onboard/step3")
    object CsoOnboardStep4 : AppDestination("cso/onboard/step4")
    object CsoOnboardStep5 : AppDestination("cso/onboard/step5")
    object CsoOnboardStep6 : AppDestination("cso/onboard/step6")
    object CsoOutletDetails : AppDestination("cso/outlet-details")
    object CsoCompliance : AppDestination("cso/compliance")
    object CsoInvoices : AppDestination("cso/invoices")
    object CsoInvoiceDetail : AppDestination("cso/invoice-detail")

    // ASE (Area Sales Executive) — L1 approver
    object AseHome : AppDestination("ase/home")

    // Outlet
    object OutletDashboard : AppDestination("outlet/dashboard")
    object OutletInvoices : AppDestination("outlet/invoices")
    object OutletInvoiceDetail : AppDestination("outlet/invoice-detail")
    object OutletUploadInvoice : AppDestination("outlet/upload-invoice")
    /** Crop & rotate review screen after camera capture */
    object OutletInvoiceCropReview : AppDestination("outlet/invoice-crop-review")
    /** ROI manual extraction: outlet/region-scan/{itemIndex}/{fieldName} */
    object OutletRegionScan : AppDestination("outlet/region-scan/{itemIndex}/{fieldName}") {
        fun route(itemIndex: Int, fieldName: String) = "outlet/region-scan/$itemIndex/$fieldName"
    }
    /** AI extraction in-progress screen (fullscreen, no top bar) */
    object OutletInvoiceProcessing : AppDestination("outlet/invoice-processing")
    /** AI extraction review & confirm screen */
    object OutletInvoiceReview : AppDestination("outlet/invoice-review")
    /** PDF preview before AI extraction — replaces crop/rotate for PDF flow */
    object OutletInvoicePdfPreview : AppDestination("outlet/invoice-pdf-preview")

    // ASM
    object AsmHome : AppDestination("asm/home")
    object AsmDashboard : AppDestination("asm/dashboard")
    object AsmOutletReview : AppDestination("asm/outlet_review")
    object AsmInvoices : AppDestination("asm/invoices")
    object AsmInvoiceDetail : AppDestination("asm/invoice_detail")
    object AsmMyTeam : AppDestination("asm/my_team")

    // Walkthrough (Retailer first-time)
    object WalkthroughWelcome : AppDestination("walkthrough/welcome")
    object WalkthroughOutletDetails : AppDestination("walkthrough/outlet-details")
    object WalkthroughAgreement : AppDestination("walkthrough/agreement")
    object WalkthroughPayment : AppDestination("walkthrough/payment")

    // Shared
    object Profile : AppDestination("profile")
    object HelpSupport : AppDestination("support/help")
    object RaiseTicket : AppDestination("support/raise-ticket")

}
