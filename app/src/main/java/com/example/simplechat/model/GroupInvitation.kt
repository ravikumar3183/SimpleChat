package com.example.simplechat.model

data class GroupInvitation(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val senderId: String = "", // Who invited you
    val receiverId: String = "" // You
)