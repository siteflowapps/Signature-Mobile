package com.siteflow.retailsync.outlet.di

import com.siteflow.retailsync.outlet.data.OutletApi
import com.siteflow.retailsync.outlet.data.OutletRepository
import org.koin.dsl.module

val outletModule = module {
    single { OutletApi(get()) }
    single { OutletRepository(get()) }
}
