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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.milesolutions.rewardify.data.FakeRepository
import com.milesolutions.rewardify.data.TaskCategory
import com.milesolutions.rewardify.ui.components.TaskCard
import com.milesolutions.rewardify.ui.components.showToast
import com.milesolutions.rewardify.ui.theme.Gray500
import com.milesolutions.rewardify.ui.theme.Gray900
import com.milesolutions.rewardify.ui.theme.Indigo700

@Composable
fun TasksScreen(
    tabNavController: NavController,
    initialCategory: TaskCategory = TaskCategory.ALL
) {
    val context = LocalContext.current
    var selectedTab by remember(initialCategory) { mutableStateOf(initialCategory) }
    val tabs = TaskCategory.values().toList()
    val visibleTasks = FakeRepository.tasks.filter {
        selectedTab == TaskCategory.ALL || it.category == selectedTab
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
                    onStart = {
                        // TODO: open task detail / tracking screen
                        val verb = if (task.progress != null) "Continuing" else "Starting"
                        context.showToast("$verb: ${task.title}")
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
