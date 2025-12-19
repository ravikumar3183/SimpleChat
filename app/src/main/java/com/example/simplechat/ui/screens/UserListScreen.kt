package com.example.simplechat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplechat.model.Connection
import com.example.simplechat.model.User
import com.example.simplechat.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    onUserClick: (String, String) -> Unit,
    onLogout: () -> Unit,
    viewModel: UserViewModel = viewModel()
) {
    val users by viewModel.users.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No other users found.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(users) { user ->
                    val connection = connections[user.userId]

                    UserItemWithStatus(
                        user = user,
                        connection = connection,
                        currentUserId = currentUserId,
                        onChatClick = { onUserClick(user.userId, user.displayName) },
                        onSendRequest = { viewModel.sendRequest(user.userId) },
                        onAccept = { viewModel.acceptRequest(user.userId) },
                        onDecline = { viewModel.removeConnection(user.userId) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun UserItemWithStatus(
    user: User,
    connection: Connection?,
    currentUserId: String,
    onChatClick: () -> Unit,
    onSendRequest: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val status = connection?.status // "pending" or "accepted" or null
    val actionBy = connection?.actionBy

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (status == "accepted") Modifier.clickable(onClick = onChatClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Push button to right
    ) {
        // --- LEFT SIDE: User Info ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Avatar
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (user.displayName.isNotEmpty()) user.displayName.take(1).uppercase() else "?",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (user.displayName.isNotEmpty()) user.displayName else "Unknown",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // --- RIGHT SIDE: Action Buttons ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                // CASE 1: ALREADY CONNECTED
                status == "accepted" -> {
                    // Implicitly handled by the row click, but we can add an arrow or text
                    Text("Chat", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }

                // CASE 2: PENDING - I SENT IT
                status == "pending" && actionBy == currentUserId -> {
                    Text("Sent", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }

                // CASE 3: PENDING - THEY SENT IT (Show Accept/Decline)
                status == "pending" && actionBy != currentUserId -> {
                    IconButton(onClick = onAccept) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
                    }
                    IconButton(onClick = onDecline) {
                        Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.Red)
                    }
                }

                // CASE 4: NO CONNECTION (Show Request Button)
                else -> {
                    Button(onClick = onSendRequest, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("Request")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}