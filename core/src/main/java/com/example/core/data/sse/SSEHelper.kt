package com.example.core.data.sse

import android.util.Log
import com.example.core.di.scopes.qualifiers.BasicAuthOkHttp
import com.example.core.utils.hasAnnotation
import com.example.core.utils.printBody
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

import retrofit2.Call
import javax.inject.Inject

class SSEHelper @Inject constructor(
    @BasicAuthOkHttp okHttpClient: OkHttpClient,
    val json: Json
) {

    val eventSource by lazy {
        EventSources.createFactory(okHttpClient)
    }

    /*
    * R = Response Event
    * E = Error Event
    * F = Failure Event
    * */

    inline fun <reified R : Any, reified E : Any, reified F : Any> requestEvents(
        callBlock: () -> Call<*>
    ) = requestEvents<R, E, F>(callBlock().request())

    inline fun <reified R : Any, reified E : Any, reified F : Any> requestEvents(call: Call<*>) =
        requestEvents<R, E, F>(call.request())

    @OptIn(InternalSerializationApi::class)
    inline fun <reified R : Any, reified E : Any, reified F : Any> requestEvents(
        request: Request
    ) = callbackFlow {
        Log.d(TAG, "requestEvents: headers: ${request.headers}")
        Log.d(TAG, "requestEvents: body: ${request.printBody()}")

        val eventSourceListener = object : EventSourceListener() {

            override fun onOpen(eventSource: EventSource, response: Response) {
                super.onOpen(eventSource, response)
                if (!isActive) return

                Log.d(TAG, "onOpen: response: $response")
                trySend(SseEvent.Opened())
            }

            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
                if (!isActive) return

                Log.d(TAG, "onClosed:")
                trySend(SseEvent.Closed())
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                super.onEvent(eventSource, id, type, data)
                if (!isActive) return

                Log.d(TAG, "onEvent: id: $id, type: $type, data: $data")

                when (type?.lowercase()) {
                    "error" -> {

                        val errorObj = if (E::class.hasAnnotation(Serializable::class)) {
                            json.decodeFromString(E::class.serializer(), data)
                        } else null ?: return

                        trySend(SseEvent.ErrorEvent(type, errorObj))
                    }

                    "message" -> {

                        val dataObj = if (R::class.hasAnnotation(Serializable::class)) {
                            json.decodeFromString(R::class.serializer(), data)
                        } else null ?: return

                        trySend(SseEvent.MessageEvent(type, dataObj))
                    }

                    else -> {
                        if (type.isNullOrBlank()) return

                        trySend(SseEvent.OtherEvent(type, data))
                    }
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                super.onFailure(eventSource, t, response)
                if (!isActive) return

                Log.d(TAG, "onFailure: t: $t, response: $response")
                val errorBody = response?.body?.let {
                    if (it.contentLength() > 0)
                        it.string()
                    else
                        null
                }

                val errorObj = errorBody?.let {
                    if (F::class.hasAnnotation(Serializable::class)) {
                        json.decodeFromString(F::class.serializer(), errorBody)
                    } else null
                }

                trySend(SseEvent.Failure<R, E, F>(t, errorObj))
            }
        }

        val source = eventSource.newEventSource(request = request, listener = eventSourceListener)

        awaitClose { source.cancel() }
    }

    companion object {
        const val TAG = "SSEHelper"
    }
}