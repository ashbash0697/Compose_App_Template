package com.example.core.data.models

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class Result<out T : Any>() {

    object EmptySuccess : Result<Nothing>()

    data class Success<out T : Any>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Exception,
        val message: String = exception.message ?: "Something went wrong"
    ) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            is EmptySuccess -> "EmptySuccess"
        }
    }

    suspend fun onSuccess(funcSuccess: suspend (T) -> Unit): Result<T> {
        if (this is Success) {
            funcSuccess(this.data)
        }
        return this
    }
    @JvmName("SuccessEmpty")
    suspend fun onEmptySuccess(funcSuccess: suspend () -> Unit): Result<T> {
        if (this is EmptySuccess || this is Success) {
            funcSuccess()
        }
        return this
    }

    @JvmName("SuccessEmptyHeaders")
    suspend fun onEmptySuccessWithHeaders(funcSuccess: suspend () -> Unit): Result<T> {
        if (this is EmptySuccess || this is Success) {
            funcSuccess()
        }
        return this
    }


    suspend fun onError(funcError: suspend (String) -> Unit): Result<T> {

        if (this is Error) {
            funcError(message)
        }
        return this
    }

    suspend fun onErrorException(funcError: suspend (Exception) -> Unit): Result<T> {
        if (this is Error) {
            funcError(exception)
        }
        return this
    }

    inline fun <L : Any> mapSuccess(crossinline transform: (T) -> L): Result<L> {
        return when (this) {
            is Success -> this.map(transform, this.data)
            is Error -> this
            is EmptySuccess -> this
        }
    }

    suspend inline fun <L : Any> mapSuspendEmptySuccess(crossinline transform: suspend () -> L): Result<L> {
        return when (this) {
            is Success -> transform().toSuccess()
            is Error -> this
            is EmptySuccess -> transform().toSuccess()
        }
    }

    inline fun <L : Any> mapEmptySuccess(crossinline transform: () -> L): Result<L> {
        return when (this) {
            is Success -> transform().toSuccess()
            is Error -> this
            is EmptySuccess -> transform().toSuccess()
        }
    }

    suspend inline fun <L : Any> suspendingMapSuccess(crossinline transform: suspend (T) -> L): Result<L> {
        return when (this) {
            is Success -> this.map(transform, this.data)
            is Error -> this
            is EmptySuccess -> this
        }
    }


    suspend inline fun <T : Any, L : Any> Result<T>.map(
        crossinline transform: suspend (T) -> L,
        data: T
    ): Result<L> {
        return Success(
            data = transform(data),
        )
    }

    inline fun <T : Any, L : Any> Result<T>.map(
        crossinline transform: (T) -> L,
        data: T
    ): Result<L> {
        return Success(
            data = transform(data),
        )
    }
}


fun <T : Any> Flow<Result<T>>.convertErrorToExceptions(): Flow<Result<T>> =
    map { result ->
        if (result is Result.Error) {
            throw result.exception
        } else {
            result
        }
    }

fun <L : Any> L.toSuccess(emailId: String = ""): Result<L> {
    return Result.Success(this)
}




