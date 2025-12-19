package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simplechat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    fun getConversationId(currentUserId: String, otherUserId: String): String {
        // Sort IDs to ensure the same ID is generated regardless of who started the chat
        return if (currentUserId < otherUserId) {
            "${currentUserId}_$otherUserId"
        } else {
            "${otherUserId}_$currentUserId"
        }
    }

    fun loadMessages(conversationId: String) {
        db.collection("chats").document(conversationId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val messageList = snapshot?.toObjects(Message::class.java) ?: emptyList()
                _messages.value = messageList
            }
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        val currentUserId = auth.currentUser?.uid ?: return

        val message = Message(
            id = db.collection("chats").document().id, // Auto ID
            senderId = currentUserId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chats").document(conversationId).collection("messages")
            .add(message)
    }

    fun getCurrentUser() = auth.currentUser?.uid
}