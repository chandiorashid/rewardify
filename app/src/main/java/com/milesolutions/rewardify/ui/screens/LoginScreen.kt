package com.milesolutions.rewardify.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.milesolutions.rewardify.ui.auth.AuthViewModel
import com.milesolutions.rewardify.ui.components.PrimaryButton
import com.milesolutions.rewardify.ui.components.RewardifyLogo
import com.milesolutions.rewardify.ui.components.RewardifyTextField
import com.milesolutions.rewardify.ui.components.SocialButton
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Emerald600
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo800
import com.milesolutions.rewardify.ui.theme.Indigo900

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState = authViewModel.uiState

    // React to auth results. Navigation is automatic: a successful login flips
    // the Supabase session status and MainActivity swaps to the main graph.
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            context.showToast("Welcome back!")
        }
    }
    LaunchedEffect(authState.errorMessage) {
        authState.errorMessage?.let {
            context.showToast(it)
            authViewModel.clearError()
        }
    }

    fun tryLogin() {
        when {
            email.isBlank() ->
                context.showToast("Please enter your email address")
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                context.showToast("Please enter a valid email address")
            password.isEmpty() ->
                context.showToast("Please enter your password")
            password.length < 6 ->
                context.showToast("Password must be at least 6 characters")
            else -> authViewModel.login(email, password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Gradient brand header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Brush.verticalGradient(listOf(Indigo800, Indigo900)))
                .padding(vertical = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RewardifyLogo(size = 64.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Rewardify",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineMedium,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Log in to continue earning",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray500
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = Gray500
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Forgot password?",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Emerald600,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            context.showToast("Enter your email above first")
                        } else {
                            // TODO: call password-reset API
                            context.showToast("Password reset link sent to $email")
                        }
                    }
            )

            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = if (authState.isLoading) "Logging in…" else "Log In",
                onClick = ::tryLogin,
                enabled = !authState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  or continue with  ",
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
                    onClick = { context.showToast("Google sign-in coming soon") },
                    modifier = Modifier.weight(1f)
                )
                SocialButton(
                    text = "Apple",
                    onClick = { context.showToast("Apple sign-in coming soon") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "New to Rewardify?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500
                )
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { navController.navigate("signup") }
                        .padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
