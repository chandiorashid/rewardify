package com.milesolutions.rewardify.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.milesolutions.rewardify.data.AdsConfig
import com.milesolutions.rewardify.data.FakeRepository
import com.milesolutions.rewardify.data.InstallOfferRow
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.data.RewardedAdManager
import com.milesolutions.rewardify.data.TaskCategory
import com.milesolutions.rewardify.data.TaskItem
import com.milesolutions.rewardify.data.TaskSubmissionRow
import com.milesolutions.rewardify.ui.components.InstallOfferCard
import com.milesolutions.rewardify.ui.components.OfferDetailDialog
import com.milesolutions.rewardify.ui.components.SectionTitle
import com.milesolutions.rewardify.ui.components.SubmissionRow
import com.milesolutions.rewardify.ui.components.TaskCard
import com.milesolutions.rewardify.ui.components.WatchAdCard
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo700
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    tabNavController: NavController,
    initialCategory: TaskCategory = TaskCategory.ALL
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember(initialCategory) { mutableStateOf(initialCategory) }
    val tabs = TaskCategory.values().toList()
    val visibleTasks = FakeRepository.tasks.filter {
        selectedTab == TaskCategory.ALL || it.category == selectedTab
    }
    // Task awaiting completion confirmation. Confirming records the earning
    // in the ledger — and automatically pays 5% to the user's referrer.
    var pendingTask by remember { mutableStateOf<TaskItem?>(null) }
    var recording by remember { mutableStateOf(false) }

    // ---- Rewarded ads --------------------------------------------------
    // One of $0.02 / $0.018 / $0.021 is granted at random for each
    // fully-watched ad. The earning lands in the Supabase ledger (with the
    // usual 5% lifetime commission to the user's referrer).
    var adReady by remember { mutableStateOf(RewardedAdManager.isReady) }
    var adBusy by remember { mutableStateOf(false) }

    fun watchAd() {
        val activity = context as? Activity
        if (activity == null) {
            context.showToast("Couldn't open the ad — try again")
            return
        }
        if (!RewardedAdManager.isReady) {
            context.showToast("Ad is loading — one moment…")
            RewardedAdManager.load(context) { ready -> adReady = ready }
            return
        }
        adBusy = true
        RewardedAdManager.show(
            activity = activity,
            onReward = {
                val reward = AdsConfig.AD_REWARDS.random()
                scope.launch {
                    val ok = runCatching {
                        ReferralRepository.recordTaskEarning(
                            reward,
                            "Rewarded ad watched"
                        )
                    }.isSuccess
                    context.showToast(
                        if (ok) "Earned $${"%.3f".format(reward)}! 🎬"
                        else "Couldn't save the reward — check your connection"
                    )
                }
            },
            onFinished = {
                adBusy = false
                adReady = false
                RewardedAdManager.load(context) { ready -> adReady = ready }
            }
        )
    }

    // ---- Install offers ------------------------------------------------
    var offers by remember { mutableStateOf<List<InstallOfferRow>>(emptyList()) }
    var submissions by remember { mutableStateOf<List<TaskSubmissionRow>>(emptyList()) }
    var detailOffer by remember { mutableStateOf<InstallOfferRow?>(null) }
    var submitting by remember { mutableStateOf(false) }

    fun refreshOffers() {
        scope.launch {
            runCatching { ReferralRepository.getActiveInstallOffers() }
                .onSuccess { offers = it }
            runCatching { ReferralRepository.getMySubmissions() }
                .onSuccess { submissions = it }
        }
    }

    // Offer waiting for the user to pick screenshots.
    var proofOffer by remember { mutableStateOf<InstallOfferRow?>(null) }
    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        val offer = proofOffer
        proofOffer = null
        if (offer == null || uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            submitting = true
            val result = runCatching {
                val bytes = uris.take(5).map { uri ->
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("Couldn't read a screenshot")
                }
                ReferralRepository.submitOfferProof(offer.id, bytes)
            }
            submitting = false
            result
                .onSuccess {
                    context.showToast("Proof submitted — pending review ✓")
                    refreshOffers()
                }
                .onFailure { e ->
                    context.showToast(
                        "Submit failed: ${e.message?.take(80) ?: "try again"}"
                    )
                }
        }
    }

    fun openStore(offer: InstallOfferRow) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, offer.store_url.toUri())
            )
        }.onFailure {
            context.showToast("Couldn't open the Play Store link")
        }
    }

    LaunchedEffect(Unit) {
        RewardedAdManager.load(context) { ready -> adReady = ready }
        refreshOffers()
    }

    pendingTask?.let { task ->
        AlertDialog(
            onDismissRequest = { if (!recording) pendingTask = null },
            title = { Text("Complete task?") },
            text = {
                Text("Mark \"${task.title}\" as complete to earn \$${"%.2f".format(task.reward)}.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            recording = true
                            val ok = runCatching {
                                ReferralRepository.recordTaskEarning(task.reward, task.title)
                            }.isSuccess
                            recording = false
                            pendingTask = null
                            context.showToast(
                                if (ok) "Earned \$${"%.2f".format(task.reward)}! 🎉"
                                else "Couldn't record the earning — check your connection and try again."
                            )
                        }
                    },
                    enabled = !recording
                ) { Text(if (recording) "Saving…" else "Complete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingTask = null },
                    enabled = !recording
                ) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineMedium,
                color = Gray900,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                // TODO: implement task search
                context.showToast("Search coming soon")
            }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Gray500)
            }
            IconButton(onClick = {
                // TODO: implement task filters
                context.showToast("Filters coming soon")
            }) {
                Icon(imageVector = Icons.Filled.Tune, contentDescription = "Filters", tint = Gray500)
            }
        }

        // Category pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tabs) { tab ->
                val selected = tab == selectedTab
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) Indigo700 else Color.White,
                    modifier = Modifier.clickable { selectedTab = tab }
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Color.White else Gray500,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Task list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rewarded ad: watch a short ad, earn $0.018–$0.021 at random.
            item {
                WatchAdCard(
                    adReady = adReady,
                    adBusy = adBusy,
                    onWatch = ::watchAd
                )
            }

            items(visibleTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onStart = { pendingTask = task }
                )
            }

            // Install & earn offers (admin-managed in Supabase).
            if (offers.isNotEmpty()) {
                item { SectionTitle("Install & earn") }
                items(offers, key = { it.id }) { offer ->
                    InstallOfferCard(
                        offer = offer,
                        submission = submissions.firstOrNull { it.offer_id == offer.id },
                        onClick = { detailOffer = offer }
                    )
                }
            }

            // Filed submissions and their review status.
            if (submissions.isNotEmpty()) {
                item { SectionTitle("My submissions") }
                items(submissions, key = { it.id }) { submission ->
                    SubmissionRow(
                        submission = submission,
                        offerName = offers.firstOrNull { it.id == submission.offer_id }?.app_name
                            ?: "Install offer"
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // Install-offer details / proof submission.
        detailOffer?.let { offer ->
            OfferDetailDialog(
                offer = offer,
                submitting = submitting,
                onDismiss = { if (!submitting) detailOffer = null },
                onOpenStore = { openStore(offer) },
                onSubmitProof = {
                    detailOffer = null
                    proofOffer = offer
                    pickImages.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )
        }
    }
}
