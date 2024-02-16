package com.example.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URL
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NetworkWatcher @Inject constructor(@ApplicationContext private val context: Context) {

    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    sealed class Status {
        class Connected(val source: Source) : Status()
        object NotConnected : Status()
        object Unknown : Status()
    }

    enum class Source {
        WIFI,
        CELLULAR,
        ETHERNET,
        BLUETOOTH,
        VPN
    }

    fun isConnected(): Boolean {
        return getConnectionStatus() is Status.Connected
    }

    fun getConnectionStatus(): Status {
        val activeNetwork = connectivityManager.activeNetwork

        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork)

        Log.d(TAG, "getConnectionStatus: $networkCapabilities")
        if (networkCapabilities != null &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ) {
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> Status.Connected(Source.WIFI)
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> Status.Connected(Source.CELLULAR)
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> Status.Connected(Source.ETHERNET)
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> Status.Connected(Source.BLUETOOTH)
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> Status.Connected(Source.VPN)
                else -> Status.NotConnected
            }
        }
        return Status.NotConnected
    }

    @ExperimentalCoroutinesApi
    fun watchNetwork(pingCheck: Boolean = false) = callbackFlow<Status> {

        trySend(getConnectionStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(Status.NotConnected)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(Status.NotConnected)
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                if (pingCheck) {
                    checkPing { isConnected ->
                        if (isConnected) {
                            trySend(getConnectionStatus())
                        }
                    }
                } else {
                    trySend(getConnectionStatus())
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    fun checkPing(result: (isConnected: Boolean) -> Unit) = CoroutineScope(Dispatchers.IO).launch {
        val result = kotlin.runCatching {
            callNetwork()
        }.onSuccess {}
            .onFailure {}
            .getOrDefault(false)

        result(result)
    }

    private suspend fun callNetwork() = suspendCancellableCoroutine<Boolean> { cont ->
        val urlConnection = URL("https://clients3.google.com/generate_204")
            .openConnection() as HttpsURLConnection

        try {
            urlConnection.setRequestProperty("User-Agent", "Android")
            urlConnection.setRequestProperty("Connection", "close")
            urlConnection.connectTimeout = 2000

            urlConnection.connect()
            cont.resume(urlConnection.responseCode == 204)
        } catch (e: Exception) {
            Log.e(TAG, "checkInternet: ", e)
            cont.resumeWithException(e)
        } finally {
            urlConnection.disconnect()
        }
    }

    companion object {
        private const val TAG = "NetworkWatcher"
    }

}