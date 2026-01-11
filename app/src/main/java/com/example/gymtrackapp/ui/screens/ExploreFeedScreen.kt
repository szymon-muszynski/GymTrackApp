package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.ui.components.OfflineBanner
import com.example.gymtrackapp.ui.components.PostCard
import com.example.gymtrackapp.ui.components.PostDetailsDialog
import com.example.gymtrackapp.ui.theme.AppBackground
import com.example.gymtrackapp.ui.theme.AppGreen
import com.example.gymtrackapp.ui.theme.AppMutedText
import com.example.gymtrackapp.ui.theme.AppShapes
import com.example.gymtrackapp.ui.theme.AppSurface
import com.example.gymtrackapp.ui.util.LocalNetworkState
import com.example.gymtrackapp.ui.util.PullToRefreshCompat
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel
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

    LaunchedEffect(Unit) {
        viewModel.refreshFirstPage()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // --- HEADER CARD ---
        ExploreFeedHeader()

        Spacer(Modifier.height(8.dp))

        // Padding poziomy dla contentu
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Column {
                OfflineBanner(networkState = networkState)

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(8.dp))

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
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
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
                                        CircularProgressIndicator(color = AppGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun ExploreFeedHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Eksploruj",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Przeglądaj treningi i postępy znajomych",
                fontSize = 14.sp,
                color = AppMutedText
            )
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
                color = Color.Black
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Znajdź znajomych i zacznij obserwować, aby widzieć ich treningi.",
                color = AppMutedText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (!refreshing && onGoToSearch != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onGoToSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape = AppShapes.button
                ) {
                    Text("Szukaj znajomych", color = Color.White)
                }
            }
        }
    }
}