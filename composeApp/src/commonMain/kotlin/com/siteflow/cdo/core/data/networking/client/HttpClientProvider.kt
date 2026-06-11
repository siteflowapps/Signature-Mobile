package com.siteflow.cdo.core.data.networking.client

import io.ktor.client.HttpClient

interface HttpClientProvider {
    fun create(): HttpClient
}

