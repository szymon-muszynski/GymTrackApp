package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.ui.components.AvatarCircle
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
import com.example.gymtrackapp.ui.viewmodel.MyProfileViewModel
import com.example.gymtrackapp.utils.NetworkMonitor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
            .background(AppBackground) // Ujednolicone tło
    ) {
        // --- HEADER CARD (Twój profil) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            shape = AppShapes.card,
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "My profile",
                        fontSize = 14.sp,
                        color = AppMutedText
                    )
                    if (error != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(text = error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }

        // Kontener na listę i banery z paddingiem
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            OfflineBanner(networkState = networkState)
            Spacer(Modifier.height(4.dp))

            // Snackbar host wewnątrz kolumny, żeby nie zasłaniał contentu
            Box(modifier = Modifier.fillMaxWidth()) {
                SnackbarHost(hostState = snackbarHostState)
            }

            Spacer(Modifier.height(8.dp))

            PullToRefreshCompat(
                isRefreshing = refreshing,
                onRefresh = {
                    if (!isOnline) {
                        scope.launch { snackbarHostState.showSnackbar("No internet connection") }
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
                                text = if (refreshing) "Refreshing…" else "No posts yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Share a workout and it will appear here.",
                                fontSize = 14.sp,
                                color = AppMutedText,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(posts, key = { it.postId }) { post ->
                            MyPostRow(
                                post = post,
                                busy = deleteBusy[post.postId] == true,
                                onOpenDetails = { detailsPost = it },
                                onDelete = {
                                    if (!isOnline) {
                                        scope.launch { snackbarHostState.showSnackbar("No internet connection") }
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
                                    CircularProgressIndicator(color = AppGreen)
                                }
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

    // Używamy PostCard, który jest zunifikowany, ale dodajemy menu
    PostCard(
        post = post,
        onOpenDetails = onOpenDetails,
        headerActions = {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    enabled = !busy,
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = AppSurface
                ) {
                    DropdownMenuItem(
                        text = { Text(if (busy) "Deleting…" else "Delete post") },
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