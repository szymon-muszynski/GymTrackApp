package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.ui.components.PostCard
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel
import com.example.gymtrackapp.ui.util.PullToRefreshCompat

@Composable
fun ExploreFeedScreen(
    viewModel: ExploreFeedViewModel,
    modifier: Modifier = Modifier,
    onGoToSearch: (() -> Unit)? = null,
    onUserClick: ((userId: String) -> Unit)? = null,
) {
    val feed by viewModel.feed.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    // automatyczny pierwszy refresh (best-effort)
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Eksploruj",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        PullToRefreshCompat(
            isRefreshing = refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (feed.isEmpty()) {
                EmptyFeedState(
                    refreshing = refreshing,
                    onGoToSearch = onGoToSearch,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feed, key = { it.postId }) { post ->
                        PostCard(
                            post = post,
                            onAuthorClick = onUserClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedState(
    refreshing: Boolean,
    onGoToSearch: (() -> Unit)?,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (refreshing) "Odświeżanie…" else "Twój feed jest pusty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Znajdź znajomych i zacznij obserwować, aby widzieć ich treningi.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!refreshing && onGoToSearch != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onGoToSearch) {
                    Text("Szukaj znajomych")
                }
            }
        }
    }
}
