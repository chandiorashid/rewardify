package com.milesolutions.rewardify.ui.screens

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
import com.milesolutions.rewardify.data.FakeRepository
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.data.TaskCategory
import com.milesolutions.rewardify.data.TaskItem
import com.milesolutions.rewardify.ui.components.TaskCard
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
            items(visibleTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onStart = { pendingTask = task }
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
