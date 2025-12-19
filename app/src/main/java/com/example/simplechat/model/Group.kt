package com.example.simplechat.model

data class Group(
    val groupId: String = "",
    val name: String = "",
    val ownerId: String = "",
    val members: List<String> = emptyList() // List of User IDs who accepted
)