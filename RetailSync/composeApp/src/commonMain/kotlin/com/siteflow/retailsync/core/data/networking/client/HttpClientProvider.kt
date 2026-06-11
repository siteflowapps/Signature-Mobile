package com.siteflow.retailsync.core.data.networking.client

import io.ktor.client.HttpClient

interface HttpClientProvider {
    fun create(): HttpClient
}
