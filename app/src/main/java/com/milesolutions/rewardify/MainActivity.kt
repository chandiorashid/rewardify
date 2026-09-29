package com.milesolutions.rewardify

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.milesolutions.rewardify.data.Supabase
import com.milesolutions.rewardify.data.SupabaseConfig
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.navigation.AuthNavHost
import com.milesolutions.rewardify.ui.navigation.MainRoot
import com.milesolutions.rewardify.ui.screens.SplashScreen
import com.milesolutions.rewardify.ui.theme.RewardifyTheme
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Cold start via the email-confirmation deep link.
        handleAuthDeepLink(intent)
        setContent {
            RewardifyTheme {
                RewardifyApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // App already running (singleTask): the confirmation link arrives here.
        handleAuthDeepLink(intent)
    }

    /**
     * Imports the Supabase session when the activity is opened with the
     * email-confirmation deep link. No-op for normal launches.
     */
    @OptIn(SupabaseInternal::class)
    private fun handleAuthDeepLink(intent: Intent?) {
        if (intent == null || !SupabaseConfig.isConfigured) return
        Supabase.client.handleDeeplinks(
            intent,
            onSessionSuccess = {
                showToast("Email verified — welcome to Rewardify! 🎉")
            },
            onError = { error ->
                showToast(error.message ?: "Could not verify your email. Please try again.")
            }
        )
    }
}

/**
 * Root UI, driven by the Supabase session status:
 * - Initializing  -> branded splash (no more login/signup flash on startup)
 * - Authenticated -> main app (bottom-nav shell)
 * - NotAuthenticated / RefreshFailure -> login/signup flow
 *
 * Because the whole tree reacts to [SessionStatus], email-confirmation deep
 * links, logins and logouts all navigate automatically — no manual routing.
 */
@Composable
private fun RewardifyApp() {
    if (!SupabaseConfig.isConfigured) {
        AuthNavHost()
        return
    }

    val sessionStatus by Supabase.client.auth.sessionStatus.collectAsState()

    // Keep showing the last resolved screen while the session is being
    // reloaded (e.g. returning from the email app), so the splash only
    // appears on the very first launch.
    var lastResolved by remember { mutableStateOf<SessionStatus?>(null) }
    val effectiveStatus = when (val status = sessionStatus) {
        is SessionStatus.Initializing -> lastResolved ?: status
        else -> status.also { lastResolved = it }
    }

    when (effectiveStatus) {
        is SessionStatus.Initializing -> SplashScreen()
        is SessionStatus.Authenticated -> MainRoot()
        is SessionStatus.NotAuthenticated,
        is SessionStatus.RefreshFailure -> AuthNavHost()
    }
}
