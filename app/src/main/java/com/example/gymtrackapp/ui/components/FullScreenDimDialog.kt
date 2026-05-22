package com.example.gymtrackapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullScreenDimDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dimAlpha: Float = 0.55f,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha)),
            contentAlignment = contentAlignment,
        ) {
            content()
        }
    }
}

