package com.milesolutions.rewardify.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milesolutions.rewardify.data.AuthRepository
import com.milesolutions.rewardify.data.SupabaseConfig
import io.github.jan.supabase.auth.exception.AuthRestException
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.launch
import java.net.UnknownHostException

data class AuthUiState(
    val isLoading: Boolean = false,
    /** Error to show as a toast (consumed via [AuthViewModel.clearError]). */
    val errorMessage: String? = null,
    /** Info to show as a toast (consumed via [AuthViewModel.clearInfo]). */
    val infoMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    // Note: no session check in init. The app's root UI observes the Supabase
    // session status directly and only shows these screens when logged out,
    // so a synchronous check here would race the async session restore.

    fun login(email: String, password: String) {
        if (!SupabaseConfig.isConfigured) {
            uiState = uiState.copy(
                errorMessage = "Supabase is not configured yet — add your URL and anon key"
            )
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                AuthRepository.signIn(email.trim(), password)
                uiState = uiState.copy(isLoading = false, isLoggedIn = true)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = e.toUserMessage())
            }
        }
    }

    fun signup(name: String, email: String, password: String) {
        if (!SupabaseConfig.isConfigured) {
            uiState = uiState.copy(
                errorMessage = "Supabase is not configured yet — add your URL and anon key"
            )
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)
            try {
                AuthRepository.signUp(name.trim(), email.trim(), password)
                // If email confirmation is enabled in Supabase, there is no
                // session yet — the user must confirm via email first.
                uiState = if (AuthRepository.isLoggedIn()) {
                    uiState.copy(isLoading = false, isLoggedIn = true)
                } else {
                    uiState.copy(
                        isLoading = false,
                        infoMessage = "Account created! Tap the confirmation link in your email — you'll be signed in automatically."
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = e.toUserMessage())
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { AuthRepository.signOut() }
            uiState = AuthUiState()
            onDone()
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun clearInfo() {
        uiState = uiState.copy(infoMessage = null)
    }

    /** Maps SDK / network exceptions to friendly messages. */
    private fun Exception.toUserMessage(): String = when (this) {
        is AuthRestException -> when {
            message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                "Wrong email or password. Please try again."
            message?.contains("User already registered", ignoreCase = true) == true ->
                "This email is already registered. Try logging in instead."
            message?.contains("Password should be", ignoreCase = true) == true ->
                "Password is too weak — use at least 6 characters."
            message?.contains("Email not confirmed", ignoreCase = true) == true ->
                "Please confirm your email first, then log in."
            else -> message?.takeIf { it.isNotBlank() } ?: "Authentication failed. Please try again."
        }
        is UnknownHostException, is SocketTimeoutException ->
            "Network error. Check your internet connection and try again."
        else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
    }
}
