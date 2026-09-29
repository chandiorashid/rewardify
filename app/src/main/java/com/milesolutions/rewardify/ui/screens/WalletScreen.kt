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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.milesolutions.rewardify.data.EarningRow
import com.milesolutions.rewardify.data.FakeRepository
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.data.Transaction
import com.milesolutions.rewardify.ui.components.RedeemDialog
import com.milesolutions.rewardify.ui.components.SectionTitle
import com.milesolutions.rewardify.ui.components.TransactionRow
import com.milesolutions.rewardify.ui.components.WithdrawDialog
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Gray100
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo100
import com.milesolutions.rewardify.ui.theme.Indigo700
import com.milesolutions.rewardify.ui.theme.Indigo800
import com.milesolutions.rewardify.ui.theme.Indigo900

@Composable
fun WalletScreen(tabNavController: NavController) {
    val context = LocalContext.current
    var showWithdraw by remember { mutableStateOf(false) }
    var showRedeem by remember { mutableStateOf(false) }

    // Real earnings ledger (falls back to demo data until the referral
    // backend is set up / the user has earnings).
    var ledgerBalance by remember { mutableStateOf<Double?>(null) }
    var ledgerTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    LaunchedEffect(Unit) {
        runCatching { ReferralRepository.getMyEarnings() }.onSuccess { rows ->
            if (rows.isNotEmpty()) {
                ledgerBalance = rows.sumOf { it.amount }
                ledgerTransactions = rows.take(10).map { e ->
                    Transaction(
                        id = e.id,
                        title = earningTitle(e),
                        date = earningDateLabel(e.created_at),
                        amount = e.amount
                    )
                }
            }
        }
    }
    val displayBalance = ledgerBalance ?: FakeRepository.availableBalance
    val displayLifetime = ledgerBalance ?: FakeRepository.lifetimeEarned
    val displayTransactions =
        if (ledgerTransactions.isNotEmpty()) ledgerTransactions
        else FakeRepository.transactions

    if (showWithdraw) {
        WithdrawDialog(
            availableBalance = displayBalance,
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
    if (showRedeem) {
        RedeemDialog(
            onDismiss = { showRedeem = false },
            onConfirm = { code ->
                showRedeem = false
                // TODO: call gift-card redemption API
                context.showToast("Gift card $code redeemed! 🎁")
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallet",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gray900,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    // TODO: open full transaction history screen
                    context.showToast("Full history coming soon")
                }) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = "History",
                        tint = Gray500
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
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
                        .background(Brush.linearGradient(listOf(Indigo700, Indigo900)))
                        .padding(22.dp)
                ) {
                    Column {
                        Text(
                            text = "Available balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Indigo100
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${"%.2f".format(displayBalance)}",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            BalanceStat(
                                label = "Pending",
                                value = "$${"%.2f".format(FakeRepository.pendingBalance)}"
                            )
                            BalanceStat(
                                label = "Lifetime earned",
                                value = "$${"%.2f".format(displayLifetime)}"
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .clickable { showWithdraw = true }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Withdraw",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Indigo800
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Quick actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WalletActionCard(
                    label = "Add payout method",
                    icon = Icons.Filled.AccountBalance,
                    onClick = {
                        // TODO: open payout-method screen
                        context.showToast("Payout methods coming soon")
                    },
                    modifier = Modifier.weight(1f)
                )
                WalletActionCard(
                    label = "Redeem gift card",
                    icon = Icons.Filled.CardGiftcard,
                    onClick = { showRedeem = true },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        item { SectionTitle("Recent transactions") }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    displayTransactions.forEachIndexed { index, tx ->
                        TransactionRow(tx = tx)
                        if (index < displayTransactions.lastIndex) {
                            HorizontalDivider(color = Gray100)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/** Human-readable label for a ledger row. */
private fun earningTitle(e: EarningRow): String = when (e.source) {
    "task" -> e.note?.takeIf { it.isNotBlank() } ?: "Task completed"
    "signup_bonus" -> "Referral welcome bonus"
    "referral_commission" -> "Referral commission (5%)"
    "withdrawal" -> "Withdrawal"
    else -> e.note?.takeIf { it.isNotBlank() } ?: "Adjustment"
}

/** "2026-09-29T10:24:00+00:00" -> "Sep 29". */
private fun earningDateLabel(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        java.time.OffsetDateTime.parse(iso).toLocalDate()
            .format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
    } catch (_: Exception) {
        iso.take(10)
    }
}

@Composable
private fun BalanceStat(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Indigo100)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun WalletActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Indigo100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Indigo700,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )
        }
    }
}
