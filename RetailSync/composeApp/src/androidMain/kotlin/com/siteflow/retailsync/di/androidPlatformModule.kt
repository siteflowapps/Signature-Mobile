package com.siteflow.retailsync.di

import com.siteflow.retailsync.core.data.auth.AndroidTokenStorage
import com.siteflow.retailsync.core.data.auth.TokenStorage
import com.siteflow.retailsync.core.data.networking.AndroidHttpClient
import com.siteflow.retailsync.core.data.networking.client.HttpClientProvider
import org.koin.dsl.module

val androidPlatformModule = module {
    single<HttpClientProvider> { AndroidHttpClient() }
    single<TokenStorage> { AndroidTokenStorage(get()) }
}
