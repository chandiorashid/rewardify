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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.milesolutions.rewardify.data.AuthRepository
import com.milesolutions.rewardify.data.FakeRepository
import com.milesolutions.rewardify.ui.components.RewardifyLogo
import com.milesolutions.rewardify.ui.components.SectionTitle
import com.milesolutions.rewardify.ui.components.TaskCard
import com.milesolutions.rewardify.ui.components.WithdrawDialog
import com.milesolutions.rewardify.ui.components.rewardifyIcon
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Emerald100
import com.milesolutions.rewardify.ui.theme.Emerald700
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo100
import com.milesolutions.rewardify.ui.theme.Indigo700
import com.milesolutions.rewardify.ui.theme.Indigo800
import com.milesolutions.rewardify.ui.theme.Indigo900

@Composable
fun HomeScreen(tabNavController: NavController) {
    val context = LocalContext.current
    var showWithdraw by remember { mutableStateOf(false) }

    if (showWithdraw) {
        WithdrawDialog(
            availableBalance = FakeRepository.availableBalance,
            onDismiss = { showWithdraw = false },
            onConfirm = { amount ->
                showWithdraw = false
                // TODO: call withdraw API, then refresh balance
                context.showToast(
                    "Withdrawal of $${"%.2f".format(amount)} requested ✓"
                )
            }
        )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RewardifyLogo(size = 40.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Rewardify",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Indigo800
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    // TODO: open notifications screen
                    context.showToast("You're all caught up! No new notifications")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = Gray500
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val greetingName = AuthRepository.currentDisplayName()
                ?.split(" ")?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: FakeRepository.userFirstName
            Text(
                text = "Hi, $greetingName 👋",
                style = MaterialTheme.typography.headlineMedium,
                color = Gray900
            )
            Text(
                text = "Ready to earn today?",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray500
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Balance hero card
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
                        .background(
                            Brush.linearGradient(listOf(Indigo700, Indigo900))
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Text(
                            text = "Total balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Indigo100
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${"%.2f".format(FakeRepository.availableBalance)}",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Emerald100
                            ) {
                                Text(
                                    text = "+$${"%.2f".format(FakeRepository.earnedThisWeek)} this week",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                modifier = Modifier.clickable { showWithdraw = true }
                            ) {
                                Text(
                                    text = "Withdraw",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo800,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Referral promo
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { tabNavController.navigate("referral") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = rewardifyIcon("card_giftcard"),
                            contentDescription = null,
                            tint = Emerald700,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Invite friends, earn 5% for life",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                        Text(
                            text = "They get a \$0.20 welcome bonus",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Gray500
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Categories
        item {
            SectionTitle("Earning categories")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FakeRepository.categories.take(2).forEach { category ->
                    CategoryTile(
                        label = category.label,
                        iconName = category.iconName,
                        onClick = {
                            tabNavController.navigate("tasks?category=${category.category.name}")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FakeRepository.categories.drop(2).forEach { category ->
                    CategoryTile(
                        label = category.label,
                        iconName = category.iconName,
                        onClick = {
                            tabNavController.navigate("tasks?category=${category.category.name}")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Featured tasks
        item { SectionTitle("Featured tasks") }
        items(FakeRepository.featuredTasks) { task ->
            TaskCard(
                task = task,
                onStart = {
                    // TODO: open task detail screen
                    context.showToast("Task started: ${task.title}")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CategoryTile(
    label: String,
    iconName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Indigo100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = rewardifyIcon(iconName),
                    contentDescription = label,
                    tint = Indigo700,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )
        }
    }
}
