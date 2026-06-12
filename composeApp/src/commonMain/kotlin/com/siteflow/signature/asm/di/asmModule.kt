package com.siteflow.signature.asm.di

import com.siteflow.signature.asm.dashboard.domain.AsmDashboardViewModel
import com.siteflow.signature.asm.invoices.domain.AsmInvoiceViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val asmModule = module {
    // AsmDashboardViewModel now needs CoroutineScope + OutletRepository + AnalyticsTracker
    single { AsmDashboardViewModel(get(), get(), get()) }
    single { AsmInvoiceViewModel(get(), get()) }
}
