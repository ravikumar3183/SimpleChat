package com.example.simplechat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplechat.viewmodel.GroupViewModel
import com.example.simplechat.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    // We need the list of friends (Accepted connections)
    val connections by userViewModel.connections.collectAsState()
    val users by userViewModel.users.collectAsState()

    // Filter users to only show accepted friends
    val friends = remember(connections, users) {
        users.filter { user ->
            connections[user.userId]?.status == "accepted"
        }
    }

    var groupName by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            groupViewModel.createGroup(groupName, selectedIds) {
                                onGroupCreated()
                            }
                        },
                        enabled = groupName.isNotBlank() && selectedIds.isNotEmpty()
                    ) {
                        Text("Create")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Group Name Input
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Members:", style = MaterialTheme.typography.titleMedium)

            // Friend List with Checkboxes
            LazyColumn {
                items(friends) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedIds.contains(friend.userId)) {
                                    selectedIds.remove(friend.userId)
                                } else {
                                    selectedIds.add(friend.userId)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedIds.contains(friend.userId),
                            onCheckedChange = { isChecked ->
                                if (isChecked) selectedIds.add(friend.userId)
                                else selectedIds.remove(friend.userId)
                            }
                        )
                        Text(text = friend.displayName.ifBlank { friend.email })
                    }
                }
            }
        }
    }
}