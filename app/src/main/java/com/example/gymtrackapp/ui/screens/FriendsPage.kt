package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModel
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel

@Composable
fun FriendsPage(
    friendsViewModel: FriendsViewModel,
    exploreFeedViewModel: ExploreFeedViewModel,
    modifier: Modifier = Modifier,
    onUserClick: ((userId: String) -> Unit)? = null,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Eksploruj") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Szukaj") }
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
