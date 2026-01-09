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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.ui.components.AvatarCircle
import com.example.gymtrackapp.ui.components.PostCard
import com.example.gymtrackapp.ui.components.PostDetailsDialog
import com.example.gymtrackapp.ui.components.OfflineBanner
import com.example.gymtrackapp.ui.util.LocalNetworkState
import com.example.gymtrackapp.ui.util.PullToRefreshCompat
import com.example.gymtrackapp.ui.viewmodel.MyProfileViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.derivedStateOf
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.gymtrackapp.utils.NetworkMonitor

@Composable
fun ProfilePage(
    viewModel: MyProfileViewModel,
    modifier: Modifier = Modifier,
) {
    val profile by viewModel.profile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val deleteBusy by viewModel.deleteBusy.collectAsState()

    var detailsPost by remember { mutableStateOf<Post?>(null) }
    if (detailsPost != null) {
        PostDetailsDialog(post = detailsPost!!, onDismiss = { detailsPost = null })
    }

    LaunchedEffect(Unit) {
        viewModel.onEnterScreen()
    }

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
            viewModel.loadMorePosts()
        }
    }

    val networkState = LocalNetworkState.current
    val isOnline = networkState == NetworkMonitor.NetworkState.OnlineValidated

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MyProfileViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayName = profile?.displayName ?: "Ja"
                val avatarColor = profile?.avatarColor ?: "#4CAF50"

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarCircle(
                        displayName = displayName,
                        avatarColor = avatarColor,
                        modifier = Modifier.size(70.dp)
                    )
                }

                Spacer(Modifier.padding(end = 14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (error != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OfflineBanner(networkState = networkState)
        Spacer(Modifier.height(8.dp))
        SnackbarHost(hostState = snackbarHostState)

        Spacer(Modifier.height(12.dp))

        PullToRefreshCompat(
            isRefreshing = refreshing,
            onRefresh = {
                if (!isOnline) {
                    scope.launch { snackbarHostState.showSnackbar("Brak połączenia z internetem") }
                } else {
                    viewModel.refresh()
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (refreshing) "Odświeżanie…" else "Brak Twoich postów",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Udostępnij trening, aby pojawił się tutaj.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts, key = { it.postId }) { post ->
                        MyPostRow(
                            post = post,
                            busy = deleteBusy[post.postId] == true,
                            onOpenDetails = { detailsPost = it },
                            onDelete = {
                                if (!isOnline) {
                                    scope.launch { snackbarHostState.showSnackbar("Brak połączenia z internetem") }
                                } else {
                                    viewModel.deletePost(post)
                                }
                            },
                        )
                    }

                    if (loadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
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
private fun MyPostRow(
    post: Post,
    busy: Boolean,
    onOpenDetails: (Post) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column {
        PostCard(
            post = post,
            onOpenDetails = onOpenDetails,
            headerActions = {
                // Wrapper, żeby menu było po prawej, w headerze karty
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        enabled = !busy,
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (busy) "Usuwanie…" else "Usuń post") },
                            onClick = {
                                menuExpanded = false
                                if (!busy) onDelete()
                            },
                            enabled = !busy
                        )
                    }
                }
            }
        )
    }
}
