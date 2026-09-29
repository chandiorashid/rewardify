package com.milesolutions.rewardify

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.milesolutions.rewardify.data.AuthRepository
import com.milesolutions.rewardify.data.EnsureProfileResult
import com.milesolutions.rewardify.data.ReferralPrefs
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.data.Supabase
import com.milesolutions.rewardify.data.SupabaseConfig
import com.google.android.gms.ads.MobileAds
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
        // Rewarded ads (Tasks screen). Safe to call before any ad load.
        MobileAds.initialize(this) {}
        // Cold start via a deep link (email confirmation or referral link).
        handleDeepLink(intent)
        setContent {
            RewardifyTheme {
                RewardifyApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // App already running (singleTask): deep links arrive here.
        handleDeepLink(intent)
    }

    /**
     * Routes incoming deep links:
     * - auth-callback -> Supabase email-confirmation handling
     * - referral      -> captures the referral code for the signup screen
     * No-op for normal launches.
     */
    @OptIn(SupabaseInternal::class)
    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != Supabase.AUTH_SCHEME || !SupabaseConfig.isConfigured) return
        when (uri.host) {
            Supabase.AUTH_HOST -> Supabase.client.handleDeeplinks(
                intent,
                onSessionSuccess = {
                    showToast("Email verified — welcome to Rewardify! 🎉")
                },
                onError = { error ->
                    showToast(error.message ?: "Could not verify your email. Please try again.")
                }
            )

            Supabase.REFERRAL_HOST -> {
                val code = uri.getQueryParameter("code")?.trim().orEmpty()
                if (code.isEmpty()) return
                if (AuthRepository.isLoggedIn()) {
                    showToast("You're already a member — share your own code to earn 5% on referrals!")
                } else {
                    ReferralPrefs.savePendingCode(this, code)
                    showToast("Referral code $code applied — sign up to get your \$0.20 bonus 🎁")
                }
            }
        }
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
        is SessionStatus.Authenticated -> {
            // First thing after any sign-in: make sure the user has a
            // referral profile (idempotent — existing users are untouched).
            // This also credits the $0.20 bonus for referred signups.
            val context = LocalContext.current
            val userId = AuthRepository.currentUserId()
            LaunchedEffect(userId) {
                val result = runCatching {
                    ReferralRepository.ensureProfile(context.applicationContext)
                }.getOrNull()
                when (result) {
                    is EnsureProfileResult.Registered -> when {
                        result.bonusCredited ->
                            context.showToast("Referral bonus: \$0.20 added to your wallet! 🎁")
                        result.invalidCode ->
                            context.showToast("That referral code wasn't recognized — no bonus applied.")
                    }
                    else -> Unit
                }
            }
            MainRoot()
        }
        is SessionStatus.NotAuthenticated,
        is SessionStatus.RefreshFailure -> AuthNavHost()
    }
}
