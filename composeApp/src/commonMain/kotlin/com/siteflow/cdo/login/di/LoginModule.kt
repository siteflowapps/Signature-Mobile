package com.siteflow.cdo.login.di

import com.siteflow.cdo.login.data.LoginApi
import com.siteflow.cdo.login.data.LoginRepository
import org.koin.dsl.module

val loginModule = module {
    single { LoginApi(get()) }
    single { LoginRepository(get()) }
}
