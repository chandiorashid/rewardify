package com.milesolutions.rewardify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.milesolutions.rewardify.data.AuthRepository
import com.milesolutions.rewardify.data.FakeRepository
import com.milesolutions.rewardify.ui.components.StatCard
import com.milesolutions.rewardify.ui.components.TierPill
import com.milesolutions.rewardify.ui.components.rewardifyIcon
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Danger500
import com.milesolutions.rewardify.ui.theme.Gray100
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo100
import com.milesolutions.rewardify.ui.theme.Indigo700
import com.milesolutions.rewardify.ui.theme.Indigo800
import com.milesolutions.rewardify.ui.theme.Indigo900

@Composable
fun ProfileScreen(
    tabNavController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = Gray900,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Indigo700, Indigo900))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AJ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        // Real Supabase user when logged in, demo data otherwise
                        val displayName =
                            AuthRepository.currentDisplayName() ?: FakeRepository.userName
                        val email =
                            AuthRepository.currentEmail() ?: FakeRepository.userEmail
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TierPill(text = FakeRepository.membershipTier)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Tasks done",
                    value = FakeRepository.tasksCompleted.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Total earned",
                    value = "$${"%.2f".format(FakeRepository.lifetimeEarned)}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Referrals",
                    value = FakeRepository.totalReferrals.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Menu
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    FakeRepository.profileMenu.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (item.isDestructive) {
                                        onLogout()
                                    } else if (item.label == "Refer & earn") {
                                        tabNavController.navigate("referral")
                                    } else {
                                        // TODO: navigate to each profile section
                                        context.showToast("${item.label} coming soon")
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Indigo100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = rewardifyIcon(item.iconName),
                                    contentDescription = null,
                                    tint = if (item.isDestructive) Danger500 else Indigo700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (item.isDestructive) Danger500 else Gray900,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = Gray500
                            )
                        }
                        if (index < FakeRepository.profileMenu.lastIndex) {
                            HorizontalDivider(
                                color = Gray100,
                                modifier = Modifier.padding(horizontal = 18.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "Rewardify v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
