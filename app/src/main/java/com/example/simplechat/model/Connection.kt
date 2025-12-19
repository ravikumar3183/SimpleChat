package com.example.simplechat.model

data class Connection(
    val user1: String = "", // The ID that is alphabetically smaller
    val user2: String = "", // The ID that is alphabetically larger
    val status: String = "pending", // "pending" or "accepted"
    val actionBy: String = "" // Who triggered the last action (e.g., who sent the request)
) {
    // Helper to check if a specific user is part of this connection
    fun hasUser(uid: String): Boolean = user1 == uid || user2 == uid
}