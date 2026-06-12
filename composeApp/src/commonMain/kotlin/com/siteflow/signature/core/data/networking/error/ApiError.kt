package com.siteflow.signature.core.data.networking.error

data class ApiError(val code: Int, val message: String) : DomainError
