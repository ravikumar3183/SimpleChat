package com.example.simplechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.simplechat.ui.screens.AiChatScreen
import com.example.simplechat.ui.screens.ChatScreen
import com.example.simplechat.ui.screens.CreateGroupScreen
import com.example.simplechat.ui.screens.GroupChatScreen
import com.example.simplechat.ui.screens.LoginScreen
import com.example.simplechat.ui.screens.SignUpScreen
import com.example.simplechat.ui.screens.UserListScreen
import com.example.simplechat.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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

        // --- 1. LOGIN ---
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

        // --- 2. SIGNUP ---
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

        // --- 3. USER LIST (HOME) ---
        composable("userList") {
            // We get AuthViewModel here just to handle Logout logic
            val authViewModel: AuthViewModel = viewModel()

            UserListScreen(
                onUserClick = { userId, chatName ->
                    navController.navigate("chat/$userId/$chatName")
                },
                onGroupClick = { groupId, groupName ->
                    navController.navigate("group_chat/$groupId/$groupName")
                },
                onCreateGroupClick = {
                    navController.navigate("create_group")
                },
                onAiChatClick = {
                    navController.navigate("ai_chat")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("userList") { inclusive = true }
                    }
                }
                // Note: We do NOT pass userViewModel or groupViewModel here.
                // The screen creates them automatically via defaults.
            )
        }

        // --- 4. AI CHAT SCREEN ---
        composable("ai_chat") {
            AiChatScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // --- 5. ONE-ON-ONE CHAT ---
        composable(
            route = "chat/{userId}/{chatName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val chatName = backStackEntry.arguments?.getString("chatName") ?: ""

            ChatScreen(
                receiverId = userId,
                receiverEmail = chatName, // Display Name
                onBack = { navController.popBackStack() }
            )
        }

        // --- 6. CREATE GROUP ---
        composable("create_group") {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onGroupCreated = {
                    navController.popBackStack()
                }
            )
        }

        // --- 7. GROUP CHAT ---
        composable(
            route = "group_chat/{groupId}/{groupName}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""

            GroupChatScreen(
                groupId = groupId,
                groupName = groupName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}