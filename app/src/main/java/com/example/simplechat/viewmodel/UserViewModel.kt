package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simplechat.model.Connection
import com.example.simplechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    // Map of OtherUserID -> Connection Object
    private val _connections = MutableStateFlow<Map<String, Connection>>(emptyMap())
    val connections = _connections.asStateFlow()

    private var userListener: ListenerRegistration? = null
    private var connectionListener: ListenerRegistration? = null

    init {
        fetchUsers()
        fetchConnections()
    }

    private fun fetchUsers() {
        userListener = db.collection("users").addSnapshotListener { snapshot, _ ->
            val userList = snapshot?.toObjects(User::class.java) ?: emptyList()
            // Filter out self
            _users.value = userList.filter { it.userId != auth.currentUser?.uid }
        }
    }

    private fun fetchConnections() {
        val myId = auth.currentUser?.uid ?: return

        // Ideally, you'd use an 'OR' query, but Firestore is strict.
        // For a simple app, we listen to the whole collection and filter client-side
        // OR you keep two listeners. Let's do client-side filter for simplicity (fine for <1000 connections).
        connectionListener = db.collection("connections").addSnapshotListener { snapshot, _ ->
            val allConnections = snapshot?.toObjects(Connection::class.java) ?: emptyList()

            // Filter only connections involving ME
            val myConnections = allConnections.filter { it.hasUser(myId) }

            // Map them by the OTHER user's ID for easy lookup
            val connectionMap = myConnections.associateBy { conn ->
                if (conn.user1 == myId) conn.user2 else conn.user1
            }
            _connections.value = connectionMap
        }
    }

    // --- ACTIONS ---

    // 1. Send Request
    fun sendRequest(otherUserId: String) {
        val myId = auth.currentUser?.uid ?: return
        val (u1, u2) = sortIds(myId, otherUserId)

        val connection = Connection(
            user1 = u1,
            user2 = u2,
            status = "pending",
            actionBy = myId
        )
        // Document ID is unique combination of IDs
        db.collection("connections").document("${u1}_${u2}").set(connection)
    }

    // 2. Accept Request
    fun acceptRequest(otherUserId: String) {
        val myId = auth.currentUser?.uid ?: return
        val (u1, u2) = sortIds(myId, otherUserId)

        db.collection("connections").document("${u1}_${u2}")
            .update("status", "accepted", "actionBy", myId)
    }

    // 3. Decline/Cancel Request (Delete the document)
    fun removeConnection(otherUserId: String) {
        val myId = auth.currentUser?.uid ?: return
        val (u1, u2) = sortIds(myId, otherUserId)

        db.collection("connections").document("${u1}_${u2}").delete()
    }

    // Helper: Sorts IDs so user1 is always the alphabetically smaller one
    private fun sortIds(id1: String, id2: String): Pair<String, String> {
        return if (id1 < id2) id1 to id2 else id2 to id1
    }

    fun getCurrentUserId() = auth.currentUser?.uid ?: ""

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        connectionListener?.remove()
    }
}