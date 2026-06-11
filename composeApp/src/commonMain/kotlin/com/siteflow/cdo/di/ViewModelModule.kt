package com.siteflow.cdo.di


import com.siteflow.cdo.core.domain.AppStartViewModel
import com.siteflow.cdo.login.domain.LoginViewModel
import com.siteflow.cdo.login.domain.OtpViewModel
import com.siteflow.cdo.outlet.walkthrough.domain.WalkthroughRepository
import com.siteflow.cdo.outlet.walkthrough.domain.WalkthroughViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { AppStartViewModel(get(), get(), get(), get()) }
    factory { LoginViewModel(get(), get(), get()) }
    factory { OtpViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { WalkthroughRepository(get(), get()) }
    single { WalkthroughViewModel(get(), get(), get(), get()) }
}

