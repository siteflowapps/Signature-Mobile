package com.siteflow.retailsync.login.di

import com.siteflow.retailsync.login.data.LoginApi
import com.siteflow.retailsync.login.data.LoginRepository
import com.siteflow.retailsync.login.domain.LoginViewModel
import com.siteflow.retailsync.login.domain.OtpViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val loginModule = module {
    single { LoginApi(get()) }
    single { LoginRepository(get()) }
    single { LoginViewModel(CoroutineScope(SupervisorJob() + Dispatchers.Main), get()) }
    single { OtpViewModel(CoroutineScope(SupervisorJob() + Dispatchers.Main), get(), get()) }
}
