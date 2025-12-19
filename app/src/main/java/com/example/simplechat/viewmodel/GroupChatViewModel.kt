package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simplechat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    fun loadGroupMessages(groupId: String) {
        db.collection("groups").document(groupId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(Message::class.java) ?: emptyList()
                _messages.value = list
            }
    }

    fun sendGroupMessage(groupId: String, text: String) {
        if (text.isBlank()) return
        val currentUserId = auth.currentUser?.uid ?: return

        // Use a generic message model, or add senderName to it if you want
        val message = Message(
            id = db.collection("groups").document().id,
            senderId = currentUserId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        db.collection("groups").document(groupId).collection("messages").add(message)
    }

    fun getCurrentUser() = auth.currentUser?.uid
}