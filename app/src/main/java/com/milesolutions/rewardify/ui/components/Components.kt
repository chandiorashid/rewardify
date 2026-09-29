package com.milesolutions.rewardify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.milesolutions.rewardify.data.TaskItem
import com.milesolutions.rewardify.data.Transaction
import com.milesolutions.rewardify.ui.theme.Danger100
import com.milesolutions.rewardify.ui.theme.Danger500
import com.milesolutions.rewardify.ui.theme.Emerald100
import com.milesolutions.rewardify.ui.theme.Emerald600
import com.milesolutions.rewardify.ui.theme.Emerald700
import com.milesolutions.rewardify.ui.theme.Gold400
import com.milesolutions.rewardify.ui.theme.Gold500
import com.milesolutions.rewardify.ui.theme.Gold600
import com.milesolutions.rewardify.ui.theme.Gray200
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray700
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo100
import com.milesolutions.rewardify.ui.theme.Indigo700
import com.milesolutions.rewardify.ui.theme.Indigo800
import com.milesolutions.rewardify.ui.theme.Indigo900

/** Maps the icon names used in [com.milesolutions.rewardify.data] to Material icons. */
fun rewardifyIcon(name: String): ImageVector = when (name) {
    "assignment" -> Icons.Filled.Assignment
    "local_offer" -> Icons.Filled.LocalOffer
    "group" -> Icons.Filled.Group
    "event_available" -> Icons.Filled.EventAvailable
    "person" -> Icons.Filled.Person
    "account_balance" -> Icons.Filled.AccountBalance
    "notifications" -> Icons.Filled.Notifications
    "card_giftcard" -> Icons.Filled.CardGiftcard
    "help" -> Icons.Filled.Help
    "shield" -> Icons.Filled.Shield
    "logout" -> Icons.Filled.Logout
    else -> Icons.Filled.Help
}

/** Brand logo: gold gift icon on an indigo gradient circle. */
@Composable
fun RewardifyLogo(size: Dp = 56.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(Indigo700, Indigo900))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CardGiftcard,
            contentDescription = "Rewardify logo",
            tint = Gold400,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

/** Full-width primary CTA button. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Indigo700,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/** Outlined social sign-in button (Google / Apple). */
@Composable
fun SocialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray900)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Rounded text field used on auth screens. */
@Composable
fun RewardifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, tint = Emerald600) }
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Indigo700,
            focusedLabelColor = Indigo700,
            cursorColor = Indigo700
        )
    )
}

/** Section heading used above content blocks. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = Gray900,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/** Emerald pill showing a reward amount, e.g. "+$2.00". */
@Composable
fun RewardBadge(amount: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Emerald100
    ) {
        Text(
            text = "+$${"%.2f".format(amount)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Emerald700,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/** A single earning task row card. */
@Composable
fun TaskCard(
    task: TaskItem,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Indigo100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = rewardifyIcon(
                        when (task.category.name) {
                            "SURVEY" -> "assignment"
                            "OFFER" -> "local_offer"
                            "REFERRAL" -> "group"
                            else -> "event_available"
                        }
                    ),
                    contentDescription = null,
                    tint = Indigo700,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900
                )
                Text(
                    text = task.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                task.progress?.let { p ->
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = p,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Emerald600,
                        trackColor = Gray200
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                RewardBadge(amount = task.reward)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (task.progress != null) "Continue" else "Start",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Indigo700,
                    modifier = Modifier.clickable(onClick = onStart)
                )
            }
        }
    }
}

/** Wallet transaction row. */
@Composable
fun TransactionRow(tx: Transaction, modifier: Modifier = Modifier) {
    val isCredit = tx.amount >= 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isCredit) Emerald100 else Gray200),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCredit) Icons.Filled.CardGiftcard else Icons.Filled.AccountBalance,
                contentDescription = null,
                tint = if (isCredit) Emerald700 else Gray500,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (tx.status != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    WithdrawalStatusPill(tx.status)
                }
            }
            Text(text = tx.date, style = MaterialTheme.typography.bodySmall, color = Gray500)
        }
        Text(
            text = (if (isCredit) "+" else "-") + "$${"%.2f".format(kotlin.math.abs(tx.amount))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCredit) Emerald600 else Gray500
        )
    }
}

/** Small colored badge for a withdrawal's payout status. */
@Composable
fun WithdrawalStatusPill(status: String) {
    val (bg, fg, label) = when (status) {
        "paid" -> Triple(Emerald100, Emerald700, "Received")
        "rejected" -> Triple(Danger100, Danger500, "Rejected")
        else -> Triple(Gold500.copy(alpha = 0.16f), Gold600, "Pending")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

/** Small stat card (tasks done / earned / referrals). */
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Indigo800
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

/** Bottom navigation bar for the four main tabs. */
@Composable
fun RewardifyBottomBar(navController: NavController) {
    val items = listOf(
        Triple("home", "Home", Icons.Filled.Home),
        Triple("tasks", "Tasks", Icons.Filled.FormatListBulleted),
        Triple("wallet", "Wallet", Icons.Filled.AccountBalanceWallet),
        Triple("profile", "Profile", Icons.Filled.Person)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
        items.forEach { (route, label, icon) ->
            val selected = currentRoute == route ||
                (route == "tasks" && currentRoute?.startsWith("tasks?") == true)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Indigo700,
                    selectedTextColor = Indigo700,
                    unselectedIconColor = Gray500,
                    unselectedTextColor = Gray500,
                    indicatorColor = Indigo100
                )
            )
        }
    }
}

/** Gold "member tier" pill used on the profile header. */
@Composable
fun TierPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Gold400.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Gold600,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
