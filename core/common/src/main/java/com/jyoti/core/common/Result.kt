package com.jyoti.core.common

/**
 * Generic wrapper used across every module boundary (network -> repository -> viewmodel)
 * so features never have to catch raw exceptions from another module.
 */
sealed class JyotiResult<out T> {
    data class Success<T>(val data: T) : JyotiResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : JyotiResult<Nothing>()
    data object Loading : JyotiResult<Nothing>()
}

inline fun <T, R> JyotiResult<T>.map(transform: (T) -> R): JyotiResult<R> = when (this) {
    is JyotiResult.Success -> JyotiResult.Success(transform(data))
    is JyotiResult.Error -> this
    JyotiResult.Loading -> JyotiResult.Loading
}
