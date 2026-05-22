package com.example.gymtrackapp.ui.util

import androidx.compose.runtime.compositionLocalOf
import com.example.gymtrackapp.utils.NetworkMonitor

val LocalNetworkState = compositionLocalOf<NetworkMonitor.NetworkState> {
    NetworkMonitor.NetworkState.Offline
}

