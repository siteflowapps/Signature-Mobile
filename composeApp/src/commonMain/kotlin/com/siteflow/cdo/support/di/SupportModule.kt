package com.siteflow.cdo.support.di

import com.siteflow.cdo.support.data.SupportApi
import com.siteflow.cdo.support.data.SupportRepository
import com.siteflow.cdo.support.domain.HelpSupportViewModel
import com.siteflow.cdo.support.domain.RaiseTicketViewModel
import org.koin.dsl.module

val supportModule = module {
    single { SupportApi(get()) }
    single { SupportRepository(get()) }
    factory { RaiseTicketViewModel(get(), get(), get()) }
    factory { HelpSupportViewModel(get(), get()) }
}
