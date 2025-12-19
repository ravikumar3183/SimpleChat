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
import androidx.compose.material.icons.filled.GroupAdd // New Icon for group creation
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplechat.model.Connection
import com.example.simplechat.model.Group
import com.example.simplechat.model.GroupInvitation
import com.example.simplechat.model.User
import com.example.simplechat.viewmodel.GroupViewModel
import com.example.simplechat.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    onUserClick: (String, String) -> Unit,
    onGroupClick: (String, String) -> Unit, // Navigate to Group Chat
    onCreateGroupClick: () -> Unit,        // Navigate to Create Group Screen
    onAiChatClick: () -> Unit, // <--- NEW CALLBACK
    onLogout: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel() // Add GroupViewModel
) {
    val users by userViewModel.users.collectAsState()
    val connections by userViewModel.connections.collectAsState()
    val currentUserId = userViewModel.getCurrentUserId()

    // Group Data
    val groupInvites by groupViewModel.invitations.collectAsState()
    val myGroups by groupViewModel.myGroups.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = onAiChatClick) {
                        Icon(
                            imageVector = Icons.Default.SmartToy, // Robot Icon
                            contentDescription = "AI Chat",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        // Floating Action Button to Create Group
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroupClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {

            // --- SECTION 1: GROUP INVITATIONS ---
            if (groupInvites.isNotEmpty()) {
                item { SectionHeader("Group Invitations") }
                items(groupInvites) { invite ->
                    GroupInviteItem(
                        invite = invite,
                        onAccept = { groupViewModel.acceptInvite(invite) },
                        onDecline = { groupViewModel.declineInvite(invite.id) }
                    )
                    HorizontalDivider()
                }
            }

            // --- SECTION 2: MY GROUPS ---
            if (myGroups.isNotEmpty()) {
                item { SectionHeader("My Groups") }
                items(myGroups) { group ->
                    GroupItem(group = group) {
                        onGroupClick(group.groupId, group.name)
                    }
                    HorizontalDivider()
                }
            }

            // --- SECTION 3: USERS / FRIENDS ---
            item { SectionHeader("Users") }

            if (users.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No other users found.", color = Color.Gray)
                    }
                }
            } else {
                items(users) { user ->
                    val connection = connections[user.userId]

                    UserItemWithStatus(
                        user = user,
                        connection = connection,
                        currentUserId = currentUserId,
                        onChatClick = { onUserClick(user.userId, user.displayName) },
                        onSendRequest = { userViewModel.sendRequest(user.userId) },
                        onAccept = { userViewModel.acceptRequest(user.userId) },
                        onDecline = { userViewModel.removeConnection(user.userId) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun GroupInviteItem(invite: GroupInvitation, onAccept: () -> Unit, onDecline: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Join Group:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = invite.groupName, style = MaterialTheme.typography.titleMedium)
        }
        Row {
            IconButton(onClick = onAccept) {
                Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
            }
            IconButton(onClick = onDecline) {
                Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.Red)
            }
        }
    }
}

@Composable
fun GroupItem(group: Group, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Group Icon Placeholder
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (group.name.isNotEmpty()) group.name.take(1).uppercase() else "G",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = group.name, style = MaterialTheme.typography.titleMedium)
    }
}

// --- EXISTING USER ITEM LOGIC (Kept exactly as is) ---
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
    val status = connection?.status
    val actionBy = connection?.actionBy

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (status == "accepted") Modifier.clickable(onClick = onChatClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Side: Info
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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

        // Right Side: Buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                status == "accepted" -> {
                    Text("Chat", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
                status == "pending" && actionBy == currentUserId -> {
                    Text("Sent", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                status == "pending" && actionBy != currentUserId -> {
                    IconButton(onClick = onAccept) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
                    }
                    IconButton(onClick = onDecline) {
                        Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.Red)
                    }
                }
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