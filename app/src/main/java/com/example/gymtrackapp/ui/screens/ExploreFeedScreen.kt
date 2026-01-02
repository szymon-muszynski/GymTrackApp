package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.data.social.model.SetSummary
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel
import com.example.gymtrackapp.ui.util.PullToRefreshCompat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ExploreFeedScreen(
    viewModel: ExploreFeedViewModel,
    modifier: Modifier = Modifier,
    onGoToSearch: (() -> Unit)? = null,
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
                        PostCard(post)
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

@Composable
private fun PostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(
                    displayName = post.authorDisplayName,
                    avatarColor = post.authorAvatarColor
                )
                Spacer(Modifier.padding(end = 10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorDisplayName,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = relativeTime(post.createdAtMs),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = post.title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            post.totalVolume?.let {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Objętość",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.padding(end = 8.dp))
                    Text(
                        text = "${formatVolume(it)} kg",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Exercises (max 3)
            post.exercises.take(3).forEach { ex ->
                Text(text = ex.name, fontWeight = FontWeight.SemiBold)
                if (ex.sets.isNotEmpty()) {
                    Text(
                        text = ex.sets.joinToString(", ") { it.format() },
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (post.exercises.size > 3) {
                Text(
                    text = "+${post.exercises.size - 3} więcej",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun SetSummary.format(): String = "${weight}kg×${reps}"

private fun formatVolume(v: Float): String = String.format(Locale.ROOT, "%.0f", v)

private fun relativeTime(createdAtMs: Long): String {
    val delta = System.currentTimeMillis() - createdAtMs
    val minutes = TimeUnit.MILLISECONDS.toMinutes(delta)
    if (minutes < 1) return "przed chwilą"
    if (minutes < 60) return "$minutes min temu"
    val hours = TimeUnit.MILLISECONDS.toHours(delta)
    if (hours < 24) return "$hours godz. temu"
    val days = TimeUnit.MILLISECONDS.toDays(delta)
    return "$days dni temu"
}

@Composable
private fun AvatarCircle(displayName: String, avatarColor: String) {
    val initials = displayName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

    val bg = runCatching { Color(android.graphics.Color.parseColor(avatarColor)) }
        .getOrDefault(Color(0xFF4CAF50))

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, Color(0x11000000), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
