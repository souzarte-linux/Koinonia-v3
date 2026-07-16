package com.koinonia.igreja.core.util

sealed interface ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>
    data class Error(val exception: Throwable, val message: String? = exception.localizedMessage) : ResultWrapper<Nothing>
    data object Loading : ResultWrapper<Nothing>
}
