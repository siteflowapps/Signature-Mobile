package com.siteflow.retailsync.invoice.di

import com.siteflow.retailsync.invoice.data.InvoiceApi
import com.siteflow.retailsync.invoice.data.InvoiceRepository
import com.siteflow.retailsync.invoice.domain.InvoiceListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val invoiceModule = module {
    single { InvoiceApi(get()) }
    single { InvoiceRepository(get()) }
    factory { InvoiceListViewModel(CoroutineScope(SupervisorJob() + Dispatchers.Main), get(), get(), get()) }
}
