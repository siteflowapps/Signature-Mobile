package com.siteflow.cdo.outlet.di

import com.siteflow.cdo.core.domain.InvoicePreprocessor
import com.siteflow.cdo.core.domain.PdfCompressor
import com.siteflow.cdo.outlet.dashboard.data.DashboardApi
import com.siteflow.cdo.outlet.dashboard.domain.OutletDashboardViewModel
import com.siteflow.cdo.outlet.invoices.data.InvoiceApi
import com.siteflow.cdo.outlet.invoices.domain.OutletInvoiceViewModel
import com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceViewModel
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
