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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.gymtrackapp.ui.util.PullToRefreshCompat
import com.example.gymtrackapp.ui.viewmodel.UserProfileViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val profile by viewModel.profile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val followingIds by viewModel.followingIds.collectAsState()
    val optimisticIsFollowing by viewModel.optimisticIsFollowing.collectAsState()
    val followBusy by viewModel.followBusy.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEnterScreen()
    }

    val targetUserId = viewModel.targetUserId
    val actualIsFollowing = optimisticIsFollowing ?: followingIds.contains(targetUserId)

    var detailsPost by remember { mutableStateOf<Post?>(null) }
    if (detailsPost != null) {
        PostDetailsDialog(post = detailsPost!!, onDismiss = { detailsPost = null })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
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
                val displayName = profile?.displayName ?: "Użytkownik"
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

                val buttonColor = if (actualIsFollowing) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                Button(
                    onClick = { viewModel.toggleFollow(actualIsFollowing) },
                    enabled = !followBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(
                        text = when {
                            followBusy -> "..."
                            actualIsFollowing -> "Obserwujesz"
                            else -> "Obserwuj"
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        PullToRefreshCompat(
            isRefreshing = refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (refreshing) "Odświeżanie…" else "Brak udostępnionych treningów",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Gdy użytkownik udostępni trening, pojawi się tutaj.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts, key = { it.postId }) { post ->
                        PostCard(
                            post = post,
                            onOpenDetails = { detailsPost = it },
                        )
                    }
                }
            }
        }
    }
}
