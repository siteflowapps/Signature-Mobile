package com.siteflow.signature.di


import com.siteflow.signature.core.domain.AppStartViewModel
import com.siteflow.signature.login.domain.LoginViewModel
import com.siteflow.signature.login.domain.OtpViewModel
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughRepository
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { AppStartViewModel(get(), get(), get(), get()) }
    factory { LoginViewModel(get(), get(), get()) }
    factory { OtpViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { WalkthroughRepository(get(), get()) }
    single { WalkthroughViewModel(get(), get(), get(), get()) }
}

