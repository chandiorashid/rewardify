package com.milesolutions.rewardify.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.ui.components.SectionTitle
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.referral.ReferralListItem
import com.milesolutions.rewardify.ui.referral.ReferralViewModel
import com.milesolutions.rewardify.ui.theme.Emerald600
import com.milesolutions.rewardify.ui.theme.Gold500
import com.milesolutions.rewardify.ui.theme.Gray100
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo100
import com.milesolutions.rewardify.ui.theme.Indigo700
import com.milesolutions.rewardify.ui.theme.Indigo800
import com.milesolutions.rewardify.ui.theme.Indigo900

@Composable
fun ReferralScreen(
    tabNavController: NavController,
    viewModel: ReferralViewModel = viewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState

    LaunchedEffect(Unit) { viewModel.refresh(context) }

    fun copyCode() {
        if (state.code.isBlank()) return
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Rewardify referral code", state.code))
        context.showToast("Referral code copied ✓")
    }

    fun shareInvite() {
        if (state.code.isBlank()) return
        val text = "Join me on Rewardify and get a " +
            "\$${"%.2f".format(ReferralRepository.SIGNUP_BONUS)} welcome bonus! 🎁\n" +
            "Use my code ${state.code} or tap:\n${state.referralLink}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share your referral link"))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top bar
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { tabNavController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Gray900
                    )
                }
                Text(
                    text = "Refer & Earn",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gray900
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Hero: the deal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Indigo700, Indigo900)))
                        .padding(22.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CardGiftcard,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Invite friends,\nearn for life",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You earn 5% of everything your referrals earn — forever. " +
                                "They get a \$${"%.2f".format(ReferralRepository.SIGNUP_BONUS)} " +
                                "welcome bonus when they join with your code.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Indigo100
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        when {
            state.isLoading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Indigo700) }
            }

            state.error != null -> item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.refresh(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo700)
                        ) { Text("Try again") }
                    }
                }
            }

            else -> {
                // Your code
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Your referral code",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray500
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.code.ifBlank { "—" },
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Indigo800
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = ::copyCode,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Copy")
                                }
                                Button(
                                    onClick = ::shareInvite,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Stats
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReferralStatCard(
                            label = "Total referrals",
                            value = state.totalReferrals.toString(),
                            icon = Icons.Filled.Group,
                            modifier = Modifier.weight(1f)
                        )
                        ReferralStatCard(
                            label = "Referral earnings",
                            value = "\$${"%.2f".format(state.referralEarnings)}",
                            icon = Icons.Filled.Payments,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Referral earnings are added to your wallet automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // How it works
                item {
                    SectionTitle("How it works")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            HowItWorksStep(
                                number = "1",
                                text = "Share your code or link with friends."
                            )
                            HowItWorksStep(
                                number = "2",
                                text = "They sign up and instantly get a " +
                                    "\$${"%.2f".format(ReferralRepository.SIGNUP_BONUS)} welcome bonus."
                            )
                            HowItWorksStep(
                                number = "3",
                                text = "You earn 5% of everything they earn — for life, automatically.",
                                last = true
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Referral list
                item { SectionTitle("Your referrals (${state.totalReferrals})") }
                if (state.referrals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No referrals yet — share your code to start earning 5% for life.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                                state.referrals.forEachIndexed { index, referral ->
                                    ReferralRow(item = referral)
                                    if (index < state.referrals.lastIndex) {
                                        HorizontalDivider(color = Gray100)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferralStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Indigo100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Indigo700,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun HowItWorksStep(number: String, text: String, last: Boolean = false) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Emerald600),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray900,
            modifier = Modifier.weight(1f)
        )
    }
    if (!last) Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun ReferralRow(item: ReferralListItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Indigo100),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Indigo700
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = Gray900
            )
            if (item.joinedLabel.isNotBlank()) {
                Text(
                    text = "Joined ${item.joinedLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
        Text(
            text = "+$${"%.2f".format(item.earnedFromThem)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Emerald600
        )
    }
}
