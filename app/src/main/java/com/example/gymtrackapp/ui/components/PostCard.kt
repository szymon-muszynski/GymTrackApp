package com.example.gymtrackapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.data.social.model.SetSummary
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    onAuthorClick: ((authorId: String) -> Unit)? = null,
    onOpenDetails: ((post: Post) -> Unit)? = null,
    headerActions: (@Composable (() -> Unit))? = null,
) {
    val clickableCard = if (onOpenDetails != null) {
        modifier
            .fillMaxWidth()
            .clickable { onOpenDetails(post) }
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = clickableCard,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                val clickableHeader = if (onAuthorClick != null) {
                    Modifier.clickable { onAuthorClick(post.authorId) }
                } else {
                    Modifier
                }

                AvatarCircle(
                    displayName = post.authorDisplayName,
                    avatarColor = post.authorAvatarColor,
                    modifier = clickableHeader
                )

                Spacer(Modifier.padding(end = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(clickableHeader)
                ) {
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

                if (headerActions != null) {
                    headerActions()
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
                        text = "Volume",
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
                    text = "+${post.exercises.size - 3} more (tap to see all)",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "Tap to view details",
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
    if (minutes < 1) return "just now"
    if (minutes < 60) return "$minutes min ago"
    val hours = TimeUnit.MILLISECONDS.toHours(delta)
    if (hours < 24) return "$hours h ago"
    val days = TimeUnit.MILLISECONDS.toDays(delta)
    return "$days days ago"
}

@Composable
fun AvatarCircle(
    displayName: String,
    avatarColor: String,
    modifier: Modifier = Modifier,
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
        .getOrDefault(Color(0xFF4CAF50))

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, Color(0x11000000), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
