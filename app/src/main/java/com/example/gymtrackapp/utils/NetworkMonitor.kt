package com.example.gymtrackapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

/**
 * Monitor stanu sieci dla Compose/VM.
 *
 * Uwaga: używamy NET_CAPABILITY_VALIDATED żeby odróżnić "jest Wi‑Fi" od "jest internet".
 */
class NetworkMonitor(private val appContext: Context) {

    sealed class NetworkState {
        object Offline : NetworkState()
        object ConnectedNoInternet : NetworkState()
        object OnlineValidated : NetworkState()
    }

    fun observe(): Flow<NetworkState> = callbackFlow {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun currentState(): NetworkState {
            val network = cm.activeNetwork ?: return NetworkState.Offline
            val caps = cm.getNetworkCapabilities(network) ?: return NetworkState.Offline

            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (!hasInternet) return NetworkState.Offline

            val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            return if (validated) NetworkState.OnlineValidated else NetworkState.ConnectedNoInternet
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentState()).isSuccess
            }

            override fun onLost(network: Network) {
                trySend(currentState()).isSuccess
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(currentState()).isSuccess
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)

        awaitClose { cm.unregisterNetworkCallback(callback) }
    }
        .onStart { emit(getCurrent()) }
        .distinctUntilChanged()

    fun getCurrent(): NetworkState {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return NetworkState.Offline
        val caps = cm.getNetworkCapabilities(network) ?: return NetworkState.Offline

        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) return NetworkState.Offline

        val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return if (validated) NetworkState.OnlineValidated else NetworkState.ConnectedNoInternet
    }
}
