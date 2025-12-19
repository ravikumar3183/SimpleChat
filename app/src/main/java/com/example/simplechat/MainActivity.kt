package com.example.simplechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.simplechat.ui.screens.ChatScreen
import com.example.simplechat.ui.screens.LoginScreen
import com.example.simplechat.ui.screens.SignUpScreen
import com.example.simplechat.ui.screens.UserListScreen
import com.example.simplechat.ui.theme.SimpleChatTheme // Make sure this exists, or use MaterialTheme directly
import com.example.simplechat.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // A simple theme wrapper
            androidx.compose.material3.MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "userList" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // ... (login and signup remain the same) ...
        composable("login") {
            LoginScreen(
                onNavigateToSignup = { navController.navigate("signup") },
                onNavigateToHome = {
                    navController.navigate("userList") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("userList") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        // --- UPDATED USER LIST NAVIGATION ---
        composable("userList") {
            val authViewModel: AuthViewModel = viewModel()
            UserListScreen(
                onUserClick = { userId, chatName ->
                    // We are now passing the NAME in the URL, not the email
                    navController.navigate("chat/$userId/$chatName")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("userList") { inclusive = true }
                    }
                }
            )
        }

        // --- UPDATED CHAT NAVIGATION ---
        composable(
            route = "chat/{userId}/{chatName}", // Changed parameter name to chatName
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val chatName = backStackEntry.arguments?.getString("chatName") ?: ""

            // We pass 'chatName' to the screen.
            // Note: If your ChatScreen parameter is still named 'receiverEmail',
            // that is fine! It just uses this string for the Top Bar Title.
            ChatScreen(
                receiverId = userId,
                receiverEmail = chatName, // We pass the name here to display it as the title
                onBack = { navController.popBackStack() }
            )
        }
    }
}