package com.milesolutions.rewardify.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Email/password authentication backed by Supabase Auth.
 * All functions are suspending — call from a coroutine / ViewModel.
 */
object AuthRepository {

    /** Registers a new user. Stores the display name in the user's metadata. */
    suspend fun signUp(name: String, email: String, password: String) {
        Supabase.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("display_name", name) }
        }
    }

    /** Signs in an existing user. */
    suspend fun signIn(email: String, password: String) {
        Supabase.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /** Signs out the current user and clears the local session. */
    suspend fun signOut() {
        Supabase.client.auth.signOut()
    }

    /** True when a valid session exists locally. */
    fun isLoggedIn(): Boolean =
        Supabase.client.auth.currentUserOrNull() != null

    fun currentEmail(): String? =
        Supabase.client.auth.currentUserOrNull()?.email

    fun currentDisplayName(): String? =
        Supabase.client.auth.currentUserOrNull()
            ?.userMetadata
            ?.get("display_name")
            ?.jsonPrimitive
            ?.content
}
