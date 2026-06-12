package com.siteflow.signature.di


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val coroutineModule = module {

    single<CoroutineScope> {
        CoroutineScope(
            Dispatchers.Main + SupervisorJob()
        )
    }
}
