package com.example.core.data.sse

import kotlinx.serialization.Serializable

/*
* R = Response Event
* E = Error Event
* F = Failure Event
* */
sealed class SseEvent<R, E, F> {
    class Opened<R, E, F> : SseEvent<R, E, F>()
    class Closed<R, E, F> : SseEvent<R, E, F>()
    data class MessageEvent<R, E, F>(val type: String, val data: R) : SseEvent<R, E, F>()
    data class ErrorEvent<R, E, F>(val type: String, val error: E) : SseEvent<R, E, F>()
    data class OtherEvent<R, E, F>(val type: String, val event: String) : SseEvent<R, E, F>()
    data class Failure<R, E, F>(val error: Throwable?, val response: F?) : SseEvent<R, E, F>()

    fun onMessageEvent(block: R.(data: R) -> Unit): SseEvent<R, E, F> {
        if (this is MessageEvent)
            data.block(data)
        return this
    }

    fun onErrorEvent(block: E.(error: E) -> Unit): SseEvent<R, E, F> {
        if (this is ErrorEvent)
            error.block(error)
        return this
    }

    fun onOtherEvent(block: (type: String, event: String) -> Unit): SseEvent<R, E, F> {
        if (this is OtherEvent)
            block(type, event)
        return this
    }

    fun onFailure(block: (error: Throwable?, response: F?) -> Unit): SseEvent<R, E, F> {
        if (this is Failure)
            block(error, response)
        return this
    }

    fun onOpened(block: () -> Unit): SseEvent<R, E, F> {
        if (this is Opened)
            block()
        return this
    }

    fun onClosed(block: () -> Unit): SseEvent<R, E, F> {
        if (this is Closed)
            block()
        return this
    }

    fun onClosedOrFailed(block: (error: Throwable?, response: F?) -> Unit): SseEvent<R, E, F> {
        when (this) {
            is Failure -> block(error, response)
            is Closed -> block(null, null)
            else -> {}
        }
        return this
    }
}

@Serializable
data class SseError(
    val code: Int = 0,
    val message: String = ""
)