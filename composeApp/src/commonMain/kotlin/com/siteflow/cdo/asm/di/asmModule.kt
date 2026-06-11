package com.siteflow.cdo.asm.di

import com.siteflow.cdo.asm.dashboard.domain.AsmDashboardViewModel
import com.siteflow.cdo.asm.invoices.domain.AsmInvoiceViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val asmModule = module {
    // AsmDashboardViewModel now needs CoroutineScope + OutletRepository + AnalyticsTracker
    single { AsmDashboardViewModel(get(), get(), get()) }
    single { AsmInvoiceViewModel(get(), get()) }
}
