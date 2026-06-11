package com.siteflow.retailsync.di

import com.siteflow.retailsync.core.domain.AppStartViewModel
import com.siteflow.retailsync.invoice.di.invoiceModule
import com.siteflow.retailsync.login.di.loginModule
import com.siteflow.retailsync.order.di.orderModule
import com.siteflow.retailsync.outlet.di.outletModule
import com.siteflow.retailsync.scanner.di.scannerModule
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule = module {
    factory { AppStartViewModel(get()) }
}

val appModules: List<Module> = listOf(
    appModule,
    coreModule,
    networkModule,
    loginModule,
    invoiceModule,
    scannerModule,
    outletModule,
    orderModule,
)
