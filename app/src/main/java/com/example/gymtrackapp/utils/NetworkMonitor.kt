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
 * Źródło prawdy dla "online validated": [NetworkStatus.isOnline].
 *
 * Dodatkowo rozróżniamy:
 * - Offline: brak aktywnej sieci / brak NET_CAPABILITY_INTERNET
 * - ConnectedNoInternet: jest aktywna sieć i NET_CAPABILITY_INTERNET, ale bez VALIDATED
 */
class NetworkMonitor(private val appContext: Context) {

    sealed class NetworkState {
        object Offline : NetworkState()
        object ConnectedNoInternet : NetworkState()
        object OnlineValidated : NetworkState()
    }

    private fun computeState(cm: ConnectivityManager): NetworkState {
        // 1) Jedno źródło prawdy: validated internet
        if (NetworkStatus.isOnline(appContext)) return NetworkState.OnlineValidated

        // 2) Skoro nie online-validated, doprecyzuj: Offline vs ConnectedNoInternet
        val network = cm.activeNetwork ?: return NetworkState.Offline
        val caps = cm.getNetworkCapabilities(network) ?: return NetworkState.Offline

        val hasInternetCap = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return if (hasInternetCap) NetworkState.ConnectedNoInternet else NetworkState.Offline
    }

    fun observe(): Flow<NetworkState> = callbackFlow {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(computeState(cm)).isSuccess
            }

            override fun onLost(network: Network) {
                trySend(computeState(cm)).isSuccess
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(computeState(cm)).isSuccess
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
        return computeState(cm)
    }
}
