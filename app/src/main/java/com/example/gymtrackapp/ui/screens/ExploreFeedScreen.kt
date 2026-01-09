package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.ui.components.OfflineBanner
import com.example.gymtrackapp.ui.components.PostCard
import com.example.gymtrackapp.ui.components.PostDetailsDialog
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel
import com.example.gymtrackapp.ui.util.LocalNetworkState
import com.example.gymtrackapp.ui.util.PullToRefreshCompat
import com.example.gymtrackapp.utils.NetworkMonitor
import kotlinx.coroutines.launch

@Composable
fun ExploreFeedScreen(
    viewModel: ExploreFeedViewModel,
    modifier: Modifier = Modifier,
    onGoToSearch: (() -> Unit)? = null,
    onUserClick: ((userId: String) -> Unit)? = null,
) {
    val feed by viewModel.feed.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val error by viewModel.error.collectAsState()

    val networkState = LocalNetworkState.current
    val isOnline = networkState == NetworkMonitor.NetworkState.OnlineValidated

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 4
        }
    }

    LaunchedEffect(shouldLoadMore, hasMore, loadingMore) {
        if (shouldLoadMore && hasMore && !loadingMore && !refreshing) {
            viewModel.loadMore()
        }
    }

    var detailsPost by remember { mutableStateOf<Post?>(null) }
    if (detailsPost != null) {
        PostDetailsDialog(post = detailsPost!!, onDismiss = { detailsPost = null })
    }

    // automatyczny pierwszy refresh (best-effort)
    LaunchedEffect(Unit) {
        viewModel.refreshFirstPage()
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

        Spacer(Modifier.height(8.dp))
        OfflineBanner(networkState = networkState)

        Spacer(Modifier.height(8.dp))
        SnackbarHost(hostState = snackbarHostState)

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        PullToRefreshCompat(
            isRefreshing = refreshing,
            onRefresh = {
                if (!isOnline) {
                    scope.launch { snackbarHostState.showSnackbar("Brak połączenia z internetem") }
                } else {
                    viewModel.refreshFirstPage()
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (feed.isEmpty()) {
                EmptyFeedState(
                    refreshing = refreshing,
                    onGoToSearch = onGoToSearch,
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feed, key = { it.postId }) { post ->
                        PostCard(
                            post = post,
                            onAuthorClick = onUserClick,
                            onOpenDetails = { detailsPost = it },
                        )
                    }

                    if (loadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF4CAF50))
                            }
                        }
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
