package com.siteflow.cdo.di

import com.siteflow.cdo.ase.di.aseModule
import com.siteflow.cdo.asm.di.asmModule
import com.siteflow.cdo.login.di.loginModule
import com.siteflow.cdo.outlet.di.outletModule
import com.siteflow.cdo.support.di.supportModule

val appModules = listOf(
    coroutineModule,
    viewModelModule,
    loginModule,
    aseModule,
    asmModule,
    outletModule,
    supportModule,
    networkModule
)
