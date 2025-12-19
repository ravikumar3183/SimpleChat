package com.example.simplechat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplechat.BuildConfig
import com.example.simplechat.model.Message
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AiChatViewModel : ViewModel() {

    // 1. Initialize Gemini
    // Use "gemini-2.5-flash" for fast, cheap responses
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // Keep chat history in memory (ViewModel)
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Hello, you are a helpful chat assistant inside an Android app.") },
            content(role = "model") { text("Understood. I am ready to help.") }
        )
    )

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1. Add User Message to UI immediately
        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            text = userText,
            senderId = "user", // Local user
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 2. Send to Gemini
                val response = chat.sendMessage(userText)
                val aiText = response.text ?: "I didn't understand that."

                // 3. Add AI Response to UI
                val aiMessage = Message(
                    id = UUID.randomUUID().toString(),
                    text = aiText,
                    senderId = "ai_bot", // ID for the bot
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                // Handle Error
                val errorMessage = Message(
                    id = UUID.randomUUID().toString(),
                    text = "Error: ${e.localizedMessage}",
                    senderId = "ai_bot",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}