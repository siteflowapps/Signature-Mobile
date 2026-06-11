package com.siteflow.retailsync.core.domain

import com.siteflow.retailsync.core.navigation.AppDestination

class AppStartViewModel(
    private val authRepository: AuthRepository
) {
    suspend fun resolveStartDestination(): String {
        return if (authRepository.isLoggedIn()) {
            AppDestination.InvoiceList.route
        } else {
            AppDestination.Login.route
        }
    }
}
