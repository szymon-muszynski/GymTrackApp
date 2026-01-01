package com.example.gymtrackapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModel

@Composable
fun FriendsPage(
    friendsViewModel: FriendsViewModel,
    modifier: Modifier = Modifier
) {
    SearchUsersScreen(viewModel = friendsViewModel, modifier = modifier)
}
