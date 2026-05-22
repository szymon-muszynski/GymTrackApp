package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.data.social.model.UserProfile
import com.example.gymtrackapp.ui.theme.AppBackground
import com.example.gymtrackapp.ui.theme.AppGreen
import com.example.gymtrackapp.ui.theme.AppMutedText
import com.example.gymtrackapp.ui.theme.AppShapes
import com.example.gymtrackapp.ui.theme.AppSurface
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModel

@Composable
fun SearchUsersScreen(
    viewModel: FriendsViewModel,
    modifier: Modifier = Modifier,
    onUserClick: ((userId: String) -> Unit)? = null,
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val followingIds by viewModel.followingIds.collectAsState()
    val optimisticOverrides by viewModel.optimisticFollowingOverrides.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val busyMap by viewModel.followBusy.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEnterScreen()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // --- HEADER CARD ---
        SearchUsersHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Enter username") },
                shape = AppShapes.button,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppGreen,
                    focusedLabelColor = AppGreen,
                    cursorColor = AppGreen
                )
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppGreen
                )
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(results, key = { it.userId }) { user ->
                    val isFollowing = optimisticOverrides[user.userId] ?: followingIds.contains(user.userId)
                    val isBusy = busyMap[user.userId] == true
                    UserRow(
                        user = user,
                        isFollowing = isFollowing,
                        isBusy = isBusy,
                        onToggleFollow = { viewModel.toggleFollow(user.userId, isFollowing) },
                        onUserClick = { onUserClick?.invoke(user.userId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchUsersHeader() {
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
                text = "Find friends",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Search for and follow other users",
                fontSize = 14.sp,
                color = AppMutedText
            )
        }
    }
}

@Composable
private fun UserRow(
    user: UserProfile,
    isFollowing: Boolean,
    isBusy: Boolean,
    onToggleFollow: () -> Unit,
    onUserClick: (() -> Unit)? = null,
) {
    Card(
        shape = AppShapes.card,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val clickMod = if (onUserClick != null) Modifier.clickable { onUserClick() } else Modifier

            AvatarCircle(
                displayName = user.displayName,
                avatarColor = user.avatarColor,
                modifier = clickMod,
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f).then(clickMod)) {
                Text(
                    text = user.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
            }

            val buttonContainerColor = if (isFollowing) AppSurface else AppGreen
            val buttonContentColor = if (isFollowing) AppGreen else Color.White
            val borderStroke = if (isFollowing) androidx.compose.foundation.BorderStroke(1.dp, AppGreen) else null

            Button(
                onClick = onToggleFollow,
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
                border = borderStroke,
                shape = AppShapes.button,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = when {
                        isBusy -> "..."
                        isFollowing -> "Following"
                        else -> "Follow"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AvatarCircle(
    displayName: String,
    avatarColor: String,
    modifier: Modifier = Modifier
) {
    val initials = displayName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

    val bg = runCatching { Color(android.graphics.Color.parseColor(avatarColor)) }
        .getOrDefault(AppGreen)

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}