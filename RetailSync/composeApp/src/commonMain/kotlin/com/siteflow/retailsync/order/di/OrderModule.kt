package com.siteflow.retailsync.order.di

import com.siteflow.retailsync.order.data.SkuApi
import com.siteflow.retailsync.order.data.SkuRepository
import com.siteflow.retailsync.order.domain.OrderCreationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val orderModule = module {
    single { SkuApi(get()) }
    single { SkuRepository(get()) }
    factory { OrderCreationViewModel(CoroutineScope(SupervisorJob() + Dispatchers.Main), get(), get(), get()) }
}
