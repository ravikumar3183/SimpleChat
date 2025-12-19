package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simplechat.model.Connection
import com.example.simplechat.model.Group
import com.example.simplechat.model.GroupInvitation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class GroupViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    // State for Group Invites sent TO me
    private val _invitations = MutableStateFlow<List<GroupInvitation>>(emptyList())
    val invitations = _invitations.asStateFlow()

    // State for Groups I am part of
    private val _myGroups = MutableStateFlow<List<Group>>(emptyList())
    val myGroups = _myGroups.asStateFlow()

    init {
        if (currentUserId != null) {
            fetchInvitations()
            fetchMyGroups()
        }
    }

    // 1. Create Group & Send Invites
    fun createGroup(groupName: String, selectedFriendIds: List<String>, onSuccess: () -> Unit) {
        if (currentUserId == null || groupName.isBlank()) return

        val groupId = UUID.randomUUID().toString()

        // A. Create the Group Object (Initially only owner is a member)
        val group = Group(
            groupId = groupId,
            name = groupName,
            ownerId = currentUserId,
            members = listOf(currentUserId)
        )

        // B. Save Group to Firestore
        db.collection("groups").document(groupId).set(group)
            .addOnSuccessListener {
                // C. Send Invitations to selected friends
                selectedFriendIds.forEach { friendId ->
                    val inviteId = UUID.randomUUID().toString()
                    val invite = GroupInvitation(
                        id = inviteId,
                        groupId = groupId,
                        groupName = groupName,
                        senderId = currentUserId,
                        receiverId = friendId
                    )
                    db.collection("group_invitations").document(inviteId).set(invite)
                }
                onSuccess()
            }
    }

    // 2. Fetch Invites
    private fun fetchInvitations() {
        if (currentUserId == null) return
        db.collection("group_invitations")
            .whereEqualTo("receiverId", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                val invites = snapshot?.toObjects(GroupInvitation::class.java) ?: emptyList()
                _invitations.value = invites
            }
    }

    // 3. Fetch My Groups (Where I am in the 'members' list)
    private fun fetchMyGroups() {
        if (currentUserId == null) return
        db.collection("groups")
            .whereArrayContains("members", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                val groups = snapshot?.toObjects(Group::class.java) ?: emptyList()
                _myGroups.value = groups
            }
    }

    // 4. Accept Invite
    fun acceptInvite(invite: GroupInvitation) {
        if (currentUserId == null) return

        // A. Add me to the group members list
        db.collection("groups").document(invite.groupId).get()
            .addOnSuccessListener { doc ->
                val group = doc.toObject(Group::class.java) ?: return@addOnSuccessListener
                val updatedMembers = group.members + currentUserId
                db.collection("groups").document(invite.groupId).update("members", updatedMembers)

                // B. Delete the invitation
                db.collection("group_invitations").document(invite.id).delete()
            }
    }

    // 5. Decline Invite
    fun declineInvite(inviteId: String) {
        db.collection("group_invitations").document(inviteId).delete()
    }
}