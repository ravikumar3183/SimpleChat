package com.example.simplechat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplechat.model.Message
import com.example.simplechat.viewmodel.ChatViewModel
import com.example.simplechat.formatTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String,
    receiverEmail: String, // This is actually the "DisplayName" now
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val currentUserId = viewModel.getCurrentUser() ?: return
    val conversationId = remember { viewModel.getConversationId(currentUserId, receiverId) }
    val messages by viewModel.messages.collectAsState()
    var text by remember { mutableStateOf("") }

    // Auto-scroll to bottom
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadMessages(conversationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = receiverEmail, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Online", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background like WhatsApp
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp) // Spacing between bubbles
            ) {
                items(messages) { message ->
                    MessageBubble(message = message, isCurrentUser = message.senderId == currentUserId)
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp), // Rounded input
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                FloatingActionButton(
                    onClick = {
                        viewModel.sendMessage(conversationId, text)
                        text = ""
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

// --- NEW BUBBLE DESIGN ---
@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.White,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 12.dp
            ),
            shadowElevation = 2.dp, // Adds a slight shadow for depth
            modifier = Modifier.widthIn(max = 300.dp) // Maximum width constraint
        ) {
            Column(modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)) {
                // Message Text
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCurrentUser) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End) // Align time to the right inside the bubble
                )
            }
        }
    }
}
