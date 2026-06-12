package com.siteflow.signature.core.data.networking.error

enum class NetworkError :
    DomainError { REQUEST_TIMEOUT, TOO_MANY_REQUESTS, NO_INTERNET, SERVER_ERROR, SERIALIZATION, UNKNOWN }