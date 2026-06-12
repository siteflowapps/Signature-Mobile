package com.siteflow.signature.support.di

import com.siteflow.signature.support.data.SupportApi
import com.siteflow.signature.support.data.SupportRepository
import com.siteflow.signature.support.domain.HelpSupportViewModel
import com.siteflow.signature.support.domain.RaiseTicketViewModel
import org.koin.dsl.module

val supportModule = module {
    single { SupportApi(get()) }
    single { SupportRepository(get()) }
    factory { RaiseTicketViewModel(get(), get(), get()) }
    factory { HelpSupportViewModel(get(), get()) }
}
