package com.siteflow.retailsync.di

import com.siteflow.retailsync.core.data.auth.IosTokenStorage
import com.siteflow.retailsync.core.data.auth.TokenStorage
import com.siteflow.retailsync.core.data.networking.IosHttpClient
import com.siteflow.retailsync.core.data.networking.client.HttpClientProvider
import org.koin.dsl.module

val iosPlatformModule = module {
    single<HttpClientProvider> { IosHttpClient() }
    single<TokenStorage> { IosTokenStorage() }
}
