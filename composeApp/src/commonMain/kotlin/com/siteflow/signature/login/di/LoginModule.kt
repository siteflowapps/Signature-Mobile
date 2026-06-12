package com.siteflow.signature.login.di

import com.siteflow.signature.login.data.LoginApi
import com.siteflow.signature.login.data.LoginRepository
import org.koin.dsl.module

val loginModule = module {
    single { LoginApi(get()) }
    single { LoginRepository(get()) }
}
