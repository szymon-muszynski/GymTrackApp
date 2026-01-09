package com.example.gymtrackapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.utils.NetworkMonitor

@Composable
fun OfflineBanner(
    networkState: NetworkMonitor.NetworkState,
    modifier: Modifier = Modifier,
) {
    val show = networkState != NetworkMonitor.NetworkState.OnlineValidated
    if (!show) return

    val text = when (networkState) {
        NetworkMonitor.NetworkState.Offline -> "Brak internetu — wyświetlamy dane z pamięci"
        NetworkMonitor.NetworkState.ConnectedNoInternet -> "Połączono z siecią bez dostępu do internetu — wyświetlamy dane z pamięci"
        NetworkMonitor.NetworkState.OnlineValidated -> ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3CD))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF856404),
        )
    }
}

