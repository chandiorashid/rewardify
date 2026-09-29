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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.milesolutions.rewardify.data.InstallOfferRow
import com.milesolutions.rewardify.data.TaskSubmissionRow
import com.milesolutions.rewardify.ui.theme.Emerald700
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo100
import com.milesolutions.rewardify.ui.theme.Indigo500
import com.milesolutions.rewardify.ui.theme.Indigo700

/**
 * Promo card for rewarded ads. [adReady] = an ad is loaded and can be shown,
 * [adBusy] = an ad is currently showing or a reward is being saved.
 */
@Composable
fun WatchAdCard(
    adReady: Boolean,
    adBusy: Boolean,
    onWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Indigo700, Indigo500))
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Watch a short ad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Earn $0.018 – $0.021 instantly",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (adReady && !adBusy) Color.White
                        else Color.White.copy(alpha = 0.35f)
                    )
                    .clickable(
                        enabled = adReady && !adBusy,
                        onClick = onWatch
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (adBusy) {
                    CircularProgressIndicator(
                        color = Indigo700,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = if (adReady) "Watch Ad" else "Loading…",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Indigo700
                    )
                }
            }
        }
    }
}

/** A single install & earn offer. Shows the user's submission status when any. */
@Composable
fun InstallOfferCard(
    offer: InstallOfferRow,
    submission: TaskSubmissionRow?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Indigo100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.GetApp,
                    contentDescription = null,
                    tint = Indigo700,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offer.app_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Install & try • +$${"%.2f".format(offer.reward)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald700,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (submission != null) {
                WithdrawalStatusPill(submission.status)
            }
        }
    }
}

/** Details + proof submission for an install offer. */
@Composable
fun OfferDetailDialog(
    offer: InstallOfferRow,
    submitting: Boolean,
    onDismiss: () -> Unit,
    onOpenStore: () -> Unit,
    onSubmitProof: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = offer.app_name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        },
        text = {
            Column {
                RewardBadgeLine("+$${"%.2f".format(offer.reward)} after manual approval")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = offer.instructions
                        ?: "Install the app, use it for 2 minutes, then submit screenshots as proof.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Your screenshots must show:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(6.dp))
                ProofBullet("The app installed on your phone")
                ProofBullet("The app's main screens you visited")
                ProofBullet("Up to 5 screenshots")
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onOpenStore, enabled = !submitting) {
                    Text("Open Play Store", fontWeight = FontWeight.Bold, color = Indigo700)
                }
                TextButton(onClick = onSubmitProof, enabled = !submitting) {
                    if (submitting) {
                        CircularProgressIndicator(
                            color = Indigo700,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        Icons.Filled.Upload,
                        contentDescription = null,
                        tint = Indigo700,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (submitting) "Uploading…" else "Submit screenshots",
                        fontWeight = FontWeight.Bold,
                        color = Indigo700
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) {
                Text("Close", color = Gray500)
            }
        }
    )
}

@Composable
private fun ProofBullet(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Indigo700)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Gray900)
    }
}

@Composable
private fun RewardBadgeLine(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Emerald700.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Emerald700
        )
    }
}

/** A user's filed submission with its review status. */
@Composable
fun SubmissionRow(
    submission: TaskSubmissionRow,
    offerName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = offerName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${submission.screenshot_urls.size} screenshot(s)",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
        WithdrawalStatusPill(submission.status)
    }
}
