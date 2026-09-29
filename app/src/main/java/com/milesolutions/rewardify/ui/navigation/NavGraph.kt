package com.milesolutions.rewardify.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.milesolutions.rewardify.data.AuthRepository
import com.milesolutions.rewardify.data.TaskCategory
import com.milesolutions.rewardify.ui.components.RewardifyBottomBar
import com.milesolutions.rewardify.ui.components.showToast
import kotlinx.coroutines.launch
import com.milesolutions.rewardify.ui.screens.HomeScreen
import com.milesolutions.rewardify.ui.screens.LoginScreen
import com.milesolutions.rewardify.ui.screens.ProfileScreen
import com.milesolutions.rewardify.ui.screens.ReferralScreen
import com.milesolutions.rewardify.ui.screens.SignupScreen
import com.milesolutions.rewardify.ui.screens.TasksScreen
import com.milesolutions.rewardify.ui.screens.WalletScreen

/**
 * Authentication flow (login <-> signup). Shown when there is no valid
 * Supabase session. Logins, signups and email-confirmation deep links flip
 * the session status, and MainActivity swaps this whole graph for [MainRoot]
 * automatically — no manual navigation needed.
 */
@Composable
fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("signup") {
            SignupScreen(navController = navController)
        }
    }
}

/** Entry point for the logged-in app. */
@Composable
fun MainRoot() {
    val rootNavController = rememberNavController()
    MainScreen(rootNavController = rootNavController)
}

/**
 * Main app shell: bottom navigation + the four tab destinations.
 */
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val tabNavController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        bottomBar = { RewardifyBottomBar(navController = tabNavController) }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(tabNavController = tabNavController) }
            composable(
                route = "tasks?category={category}",
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = TaskCategory.ALL.name
                    }
                )
            ) { backStackEntry ->
                val categoryName =
                    backStackEntry.arguments?.getString("category") ?: TaskCategory.ALL.name
                val category = runCatching { TaskCategory.valueOf(categoryName) }
                    .getOrDefault(TaskCategory.ALL)
                TasksScreen(
                    tabNavController = tabNavController,
                    initialCategory = category
                )
            }
            composable("wallet") { WalletScreen(tabNavController = tabNavController) }
            // Referral hub: opened from Profile ("Refer & earn") and the Home
            // promo card. Not a bottom tab — reached via explicit navigation.
            composable("referral") { ReferralScreen(tabNavController = tabNavController) }
            composable("profile") {
                ProfileScreen(
                    tabNavController = tabNavController,
                    onLogout = {
                        scope.launch {
                            runCatching { AuthRepository.signOut() }
                            context.showToast("Logged out. See you soon! 👋")
                            // No manual navigation: the session status flips to
                            // NotAuthenticated and MainActivity shows AuthNavHost.
                        }
                    }
                )
            }
        }
    }
}
