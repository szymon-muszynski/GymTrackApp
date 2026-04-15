package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.ui.theme.AppBackground
import com.example.gymtrackapp.ui.theme.AppGreen
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModel

@Composable
fun FriendsPage(
    friendsViewModel: FriendsViewModel,
    exploreFeedViewModel: ExploreFeedViewModel,
    modifier: Modifier = Modifier,
    onUserClick: ((userId: String) -> Unit)? = null,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(paddingValues)
        ) {
            // --- Tabs ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FriendsTabButton(
                    text = "Explore",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                FriendsTabButton(
                    text = "Search",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            when (selectedTab) {
                0 -> ExploreFeedScreen(
                    viewModel = exploreFeedViewModel,
                    modifier = Modifier.fillMaxSize(),
                    onGoToSearch = { selectedTab = 1 },
                    onUserClick = onUserClick,
                )
                1 -> SearchUsersScreen(
                    viewModel = friendsViewModel,
                    modifier = Modifier.fillMaxSize(),
                    onUserClick = onUserClick,
                )
            }
        }
    }
}

@Composable
private fun FriendsTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) AppGreen else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF757575)

    // ZMIANA: Usunięto sztywne height(40.dp) i contentPadding(0.dp).
    // Dodano padding vertical 12.dp, żeby pasowało do PlannerPage.
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(50),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(2.dp) else ButtonDefaults.buttonElevation(0.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}