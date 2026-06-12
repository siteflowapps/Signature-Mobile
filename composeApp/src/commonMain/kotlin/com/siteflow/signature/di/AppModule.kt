package com.siteflow.signature.di

import com.siteflow.signature.cso.di.csoModule
import com.siteflow.signature.asm.di.asmModule
import com.siteflow.signature.login.di.loginModule
import com.siteflow.signature.outlet.di.outletModule
import com.siteflow.signature.support.di.supportModule

val appModules = listOf(
    coroutineModule,
    viewModelModule,
    loginModule,
    csoModule,
    asmModule,
    outletModule,
    supportModule,
    networkModule
)
