package com.milesolutions.rewardify.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.milesolutions.rewardify.data.ReferralPrefs
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.ui.auth.AuthViewModel
import com.milesolutions.rewardify.ui.components.PrimaryButton
import com.milesolutions.rewardify.ui.components.RewardifyLogo
import com.milesolutions.rewardify.ui.components.RewardifyTextField
import com.milesolutions.rewardify.ui.components.SocialButton
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Emerald600
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo700

@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // Pre-filled when the user arrived via a referral link.
    var referralCode by remember { mutableStateOf(ReferralPrefs.getPendingCode(context) ?: "") }
    var agreed by remember { mutableStateOf(false) }
    val authState = authViewModel.uiState

    // React to auth results. Navigation is automatic: a successful signup flips
    // the Supabase session status and MainActivity swaps to the main graph.
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            context.showToast("Account created! Welcome to Rewardify 🎉")
        }
    }
    LaunchedEffect(authState.errorMessage) {
        authState.errorMessage?.let {
            context.showToast(it)
            authViewModel.clearError()
        }
    }
    LaunchedEffect(authState.infoMessage) {
        authState.infoMessage?.let {
            context.showToast(it)
            authViewModel.clearInfo()
            // Email confirmation required — send them to login to sign in afterwards
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

    fun trySignup() {
        when {
            name.isBlank() ->
                context.showToast("Please enter your full name")
            email.isBlank() ->
                context.showToast("Please enter your email address")
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                context.showToast("Please enter a valid email address")
            password.length < 6 ->
                context.showToast("Password must be at least 6 characters")
            password != confirmPassword ->
                context.showToast("Passwords do not match")
            !agreed ->
                context.showToast("Please accept the Terms of Service to continue")
            else -> {
                // Persist the code now — after signup there may be no session
                // yet (email confirmation), and the profile is registered on
                // first sign-in.
                val code = referralCode.trim()
                if (code.isNotEmpty()) ReferralPrefs.savePendingCode(context, code)
                else ReferralPrefs.clearPendingCode(context)
                authViewModel.signup(name, email, password)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RewardifyLogo(size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Rewardify",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Indigo700
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineMedium,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Start earning rewards in minutes",
            style = MaterialTheme.typography.bodyLarge,
            color = Gray500
        )
        Spacer(modifier = Modifier.height(24.dp))

        RewardifyTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full name",
            leadingIcon = Icons.Filled.Person,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(14.dp))
        RewardifyTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email address",
            leadingIcon = Icons.Filled.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(14.dp))
        RewardifyTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Filled.Lock,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(14.dp))
        RewardifyTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password",
            leadingIcon = Icons.Filled.Lock,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(14.dp))
        RewardifyTextField(
            value = referralCode,
            onValueChange = { referralCode = it.uppercase().filter { c -> c.isLetterOrDigit() } },
            label = "Referral code (optional)",
            leadingIcon = Icons.Filled.CardGiftcard,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        if (referralCode.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You'll get a \$${"%.2f".format(ReferralRepository.SIGNUP_BONUS)} welcome bonus 🎁",
                style = MaterialTheme.typography.bodySmall,
                color = Emerald600
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                colors = CheckboxDefaults.colors(checkedColor = Emerald600)
            )
            Text(
                text = "I agree to the Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                modifier = Modifier.clickable { agreed = !agreed }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = if (authState.isLoading) "Creating account…" else "Sign Up",
            onClick = ::trySignup,
            containerColor = Emerald600,
            enabled = !authState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  or sign up with  ",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialButton(
                text = "Google",
                onClick = { context.showToast("Google sign-up coming soon") },
                modifier = Modifier.weight(1f)
            )
            SocialButton(
                text = "Apple",
                onClick = { context.showToast("Apple sign-up coming soon") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
            Text(
                text = "Log in",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Emerald600,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
