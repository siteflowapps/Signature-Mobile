package com.siteflow.signature.di

import com.siteflow.signature.core.data.auth.AuthInterceptor
import com.siteflow.signature.core.data.auth.TokenRefreshManager
import com.siteflow.signature.core.data.config.AppEnvironment
import com.siteflow.signature.core.data.config.ConfigApi
import com.siteflow.signature.core.data.config.EnvironmentManager
import com.siteflow.signature.core.data.networking.client.HttpClientProvider
import com.siteflow.signature.core.domain.AuthRepository
import com.siteflow.signature.core.domain.SessionManager
import com.siteflow.signature.shared.data.PayoutApi
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {

    single { AuthInterceptor(get()) }
    single {
        AuthRepository(get()).also { authRepo ->
            // Initialize SessionManager with AuthRepository
            SessionManager.initialize(authRepo)

            // 🌍 Load persisted environment — if none saved, default from the
            // build flavor (debug/QA -> QA, uat -> UAT, release/PROD -> PROD) so
            // a fresh install doesn't point at PROD.
            val tokenStorage = get<com.siteflow.signature.core.data.auth.TokenStorage>()
            val savedEnv = tokenStorage.getEnvironment()
                ?.let { AppEnvironment.fromKey(it) }
                ?: AppEnvironment.fromBuild(com.siteflow.signature.buildEnvironment)
            EnvironmentManager.initialize(savedEnv)
        }
    }

    // 🔐 Token refresh manager — uses raw HttpClient (no AuthInterceptor)
    single {
        TokenRefreshManager(get(), get()).also { manager ->
            TokenRefreshManager.initialize(manager)
        }
    }

    single<HttpClient> {
        // 🔐 Ensure TokenRefreshManager is initialized before any request can be made.
        // It is not injected anywhere else, so without this line Koin never constructs it
        // and TokenRefreshManager.instance stays null — causing every 401 to force-logout
        // instead of silently refreshing.
        get<TokenRefreshManager>()

        get<HttpClientProvider>()
            .create()
            .config {
                // 🔐 Bearer token auto-attach
                install(get<AuthInterceptor>().plugin)

                // 📝 Full network logging — filter by tag "OCR_NETWORK" in Logcat / Xcode console
                install(io.ktor.client.plugins.logging.Logging) {
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            println("[OCR_NETWORK] $message")
                        }
                    }
                    level = io.ktor.client.plugins.logging.LogLevel.ALL
                }
            }
    }

    // Shared API — available to all roles
    single { PayoutApi(get()) }
    single { ConfigApi(get()) }
}
