package com.siteflow.retailsync.core.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object VerifyOtp : AppDestination("verify-otp/{mobileNumber}") {
        fun createRoute(mobileNumber: String) = "verify-otp/$mobileNumber"
    }
    data object InvoiceList : AppDestination("invoices")
    data object Scanner : AppDestination("scanner")
    data object OrderCreation : AppDestination("order-creation/{outletId}/{outletName}") {
        fun createRoute(outletId: String, outletName: String) =
            "order-creation/$outletId/$outletName"
    }
    data object OrderSuccess : AppDestination("order-success/{orderId}") {
        fun createRoute(orderId: String) = "order-success/$orderId"
    }
}
