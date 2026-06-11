package com.siteflow.retailsync.di

import com.siteflow.retailsync.core.data.auth.AuthInterceptor
import com.siteflow.retailsync.core.data.auth.TokenStorage
import com.siteflow.retailsync.core.data.networking.client.HttpClientProvider
import com.siteflow.retailsync.core.domain.AuthRepository
import com.siteflow.retailsync.core.domain.SessionManager
import io.ktor.client.HttpClient
import org.koin.dsl.module

val coreModule = module {
    single { AuthInterceptor(get()) }
    single { AuthRepository(get()) }
    single {
        SessionManager.initialize(get())
        SessionManager
    }
}

val networkModule = module {
    single<HttpClient> {
        val provider = get<HttpClientProvider>()
        val interceptor = get<AuthInterceptor>()
        provider.create().config {
            install(interceptor.plugin)
            install(io.ktor.client.plugins.logging.Logging) {
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println("[NETWORK] $message")
                    }
                }
                level = io.ktor.client.plugins.logging.LogLevel.HEADERS
            }
        }
    }
}
