package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.gymtrackapp.ui.components.PostCard
import com.example.gymtrackapp.ui.components.PostDetailsDialog
import com.example.gymtrackapp.ui.theme.AppBackground
import com.example.gymtrackapp.ui.theme.AppGreen
import com.example.gymtrackapp.ui.theme.AppMutedText
import com.example.gymtrackapp.ui.theme.AppShapes
import com.example.gymtrackapp.ui.theme.AppSurface
import com.example.gymtrackapp.ui.util.PullToRefreshCompat
import com.example.gymtrackapp.ui.viewmodel.UserProfileViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    modifier: Modifier = Modifier,
) {
    val profile by viewModel.profile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val optimisticIsFollowing by viewModel.optimisticIsFollowing.collectAsState()
    val followBusy by viewModel.followBusy.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val error by viewModel.error.collectAsState()

    val followingIds by viewModel.followingIds.collectAsState()

    LaunchedEffect(viewModel.targetUserId) {
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

    var detailsPost by remember { mutableStateOf<Post?>(null) }
    if (detailsPost != null) {
        PostDetailsDialog(post = detailsPost!!, onDismiss = { detailsPost = null })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground) // Ujednolicone tło
    ) {
        // --- HEADER CARD (Profil Innego Użytkownika) ---
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
                val displayName = profile?.displayName ?: "User"
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
                    if (error != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(text = error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Przycisk Obserwuj
                    val isFollowingFromRoom = followingIds.contains(viewModel.targetUserId)
                    val isFollowing = optimisticIsFollowing ?: isFollowingFromRoom

                    // Stylistyka zgodna z SearchUsersScreen:
                    // Obserwujesz -> Biały przycisk z zielonym obrysem i tekstem
                    // Nie obserwujesz -> Zielony przycisk z białym tekstem

                    val containerColor = if (isFollowing) AppSurface else AppGreen
                    val contentColor = if (isFollowing) AppGreen else Color.White
                    val border = if (isFollowing) BorderStroke(1.dp, AppGreen) else null

                    Button(
                        onClick = { viewModel.toggleFollow(isFollowing) },
                        enabled = !followBusy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        ),
                        border = border,
                        shape = AppShapes.button,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = when {
                                followBusy -> "..."
                                isFollowing -> "Following"
                                else -> "Follow"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Kontener na listę
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            PullToRefreshCompat(
                isRefreshing = refreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                if (posts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (refreshing) "Refreshing…" else "No shared workouts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "When the user shares a workout, it will show up here.",
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
                            PostCard(
                                post = post,
                                onOpenDetails = { detailsPost = it },
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