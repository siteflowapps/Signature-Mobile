package com.siteflow.cdo.core.navigation

sealed class AppDestination(val route: String) {

    // Auth (shared)
    object Login : AppDestination("login")
    object VerifyOtp : AppDestination("verify-otp")
    object RoleSelect : AppDestination("role-select")

    // ASE
    object AseHome : AppDestination("ase/home")
    object AseDashboard : AppDestination("ase/dashboard")
    object AseOnboardOutlet : AppDestination("ase/onboard")
    object AseOnboardStep2 : AppDestination("ase/onboard/step2")
    object AseOnboardStep3 : AppDestination("ase/onboard/step3")
    object AseOnboardStep4 : AppDestination("ase/onboard/step4")
    object AseOnboardStep5 : AppDestination("ase/onboard/step5")
    object AseOnboardStep6 : AppDestination("ase/onboard/step6")
    object AseOutletDetails : AppDestination("ase/outlet-details")
    object AseCompliance : AppDestination("ase/compliance")
    object AseInvoices : AppDestination("ase/invoices")
    object AseInvoiceDetail : AppDestination("ase/invoice-detail")

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
