package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simplechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun signup(email: String, pass: String, name: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                // Save the name to Firestore
                val user = User(
                    userId = result.user!!.uid,
                    email = email,
                    displayName = name // Save it here
                )
                db.collection("users").document(user.userId).set(user)
                    .addOnSuccessListener { _authState.value = AuthState.Success }
                    .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error") }
            }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Signup Failed") }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { _authState.value = AuthState.Success }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Login Failed") }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}