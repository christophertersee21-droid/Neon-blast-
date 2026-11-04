package com.example.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.data.model.UserTaskProgress
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSurface
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TasksScreen(
    user: UserProfile,
    tasks: List<TaskItem>,
    userTasks: List<UserTaskProgress>,
    repository: FirestoreRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("All", "Daily", "Weekly", "Special", "Referral")

    val filteredTasks = tasks.filter { task ->
        when (selectedCategoryIndex) {
            0 -> true
            1 -> task.category.equals("daily", ignoreCase = true)
            2 -> task.category.equals("weekly", ignoreCase = true)
            3 -> task.category.equals("special", ignoreCase = true)
            4 -> task.category.equals("referral", ignoreCase = true)
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "MISSIONS & TASKS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = NeonCyan
            )
            Text(
                text = "Complete gameplay tasks and invite friends to earn withdrawable rewards and coins.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = NeonSurface,
                contentColor = NeonCyan,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                        color = NeonCyan
                    )
                }
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedCategoryIndex == index) NeonCyan else TextSecondary,
                                fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Referral Card if in Referral or All tab
        if (selectedCategoryIndex == 0 || selectedCategoryIndex == 4) {
            item {
                NeonCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonMagenta
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Your Referral Code",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonMagenta
                                )
                                Text(
                                    text = "Earn ₦20.00 eligible balance for every valid invite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(user.referralCode))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = NeonMagenta
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonSurface)
                                .border(1.dp, NeonMagenta.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.referralCode,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp
                                ),
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Task Items
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks in this category.", color = TextSecondary)
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val progressDoc = userTasks.find { it.taskId == task.id }
                val current = progressDoc?.currentProgress ?: 0L
                val target = task.target.coerceAtLeast(1L)
                val isClaimed = progressDoc?.isClaimed == true
                val isCompleted = current >= target || progressDoc?.isCompleted == true
                val progressFraction = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)

                NeonCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isClaimed) NeonCyan.copy(alpha = 0.3f) else if (isCompleted) NeonGreen else NeonPurple
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isClaimed) Icons.Default.CheckCircle else Icons.Default.Task,
                                    contentDescription = "Task Icon",
                                    tint = if (isClaimed) NeonGreen else if (isCompleted) NeonGreen else NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = task.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonPurple
                                    )
                                }
                            }

                            // Reward Pill
                            Column(horizontalAlignment = Alignment.End) {
                                if (task.rewardEligible > 0) {
                                    Text(
                                        text = String.format(Locale.US, "+₦%.2f", task.rewardEligible),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NeonGreen
                                    )
                                }
                                if (task.rewardCoins > 0) {
                                    Text(
                                        text = "+${task.rewardCoins} 🪙",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonGold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar & Claim Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (isCompleted) NeonGreen else NeonCyan,
                                    trackColor = Color(0xFF1E2342)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$current / $target (${(progressFraction * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            NeonButton(
                                text = when {
                                    isClaimed -> "Claimed"
                                    isCompleted -> "Claim"
                                    else -> "In Progress"
                                },
                                enabled = isCompleted && !isClaimed,
                                glowColor = if (isCompleted && !isClaimed) NeonGreen else NeonCyan,
                                onClick = {
                                    scope.launch {
                                        repository.claimTaskReward(task)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
