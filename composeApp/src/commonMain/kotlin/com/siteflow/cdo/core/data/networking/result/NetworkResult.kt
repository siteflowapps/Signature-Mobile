package com.siteflow.cdo.core.data.networking.result

import com.siteflow.cdo.core.data.networking.error.DomainError

sealed interface NetworkResult<out D, out E> {
    data class Success<out D>(val data: D) : NetworkResult<D, Nothing>
    data class Error<out Err : DomainError>(val error: Err) : NetworkResult<Nothing, Err>
}

inline fun <T, E : DomainError, R> NetworkResult<T, E>.map(map: (T) -> R): NetworkResult<R, E> {
    return when (this) {
        is NetworkResult.Error -> NetworkResult.Error(error)
        is NetworkResult.Success -> NetworkResult.Success(map(data))
    }
}

inline fun <T, E : DomainError> NetworkResult<T, E>.onSuccess(action: (T) -> Unit): NetworkResult<T, E> {
    return when (this) {
        is NetworkResult.Error -> this
        is NetworkResult.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : DomainError> NetworkResult<T, E>.onError(action: (E) -> Unit): NetworkResult<T, E> {
    return when (this) {
        is NetworkResult.Error -> {
            action(error)
            this
        }

        is NetworkResult.Success -> this
    }
}



