package com.siteflow.signature.outlet.di

import com.siteflow.signature.core.domain.InvoicePreprocessor
import com.siteflow.signature.core.domain.PdfCompressor
import com.siteflow.signature.outlet.dashboard.data.DashboardApi
import com.siteflow.signature.outlet.dashboard.domain.OutletDashboardViewModel
import com.siteflow.signature.outlet.invoices.data.InvoiceApi
import com.siteflow.signature.outlet.invoices.domain.OutletInvoiceViewModel
import com.siteflow.signature.outlet.invoices.domain.UploadInvoiceViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val outletModule = module {
    singleOf(::DashboardApi)
    single { OutletDashboardViewModel(get(), get(), get()) }
    single { OutletInvoiceViewModel(get(), get()) }
    singleOf(::InvoiceApi)
    single {
        UploadInvoiceViewModel(
            invoicePreprocessor = get<InvoicePreprocessor>(),
            pdfCompressor = get<PdfCompressor>(),
            invoiceApi = get(),
            authRepository = get(),
            analytics = get()
        )
    }
}
