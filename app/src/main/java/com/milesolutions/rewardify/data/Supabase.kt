package com.milesolutions.rewardify.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Shared Supabase client (Auth plugin installed). Created lazily on first use.
 *
 * Deep-link email confirmation: the [Auth] config below declares the custom
 * scheme/host the app is opened with when the user taps the "Confirm email
 * address" link. The SDK then uses `scheme://host` as the redirect URL for
 * sign-up, and [io.github.jan.supabase.auth.handleDeeplinks] (called from
 * MainActivity) imports the session from that link automatically.
 *
 * This must match BOTH:
 * 1. The deep-link intent-filter in AndroidManifest.xml
 * 2. The Redirect URLs allow-list in the Supabase dashboard under
 *    Authentication -> URL Configuration.
 */
object Supabase {

    const val AUTH_SCHEME = "com.milesolutions.rewardify"
    const val AUTH_HOST = "auth-callback"
    const val AUTH_REDIRECT_URL = "$AUTH_SCHEME://$AUTH_HOST"

    /**
     * Referral deep links look like `com.milesolutions.rewardify://referral?code=ABC123`.
     * Tapping one (with the app installed) opens the app and pre-fills the
     * code on the signup screen. Must match the intent-filter in
     * AndroidManifest.xml.
     */
    const val REFERRAL_HOST = "referral"

    fun referralLink(code: String): String = "$AUTH_SCHEME://$REFERRAL_HOST?code=$code"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                scheme = AUTH_SCHEME
                host = AUTH_HOST
                defaultRedirectUrl = AUTH_REDIRECT_URL
            }
            install(Postgrest)
            install(Storage)
        }
    }
}
