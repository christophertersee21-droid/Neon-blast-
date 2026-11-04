package com.example.ui.screens.admin

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.components.NeonGalaxyBackground
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.theme.DangerNeon
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBackground
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
fun AdminDashboardScreen(
    users: List<UserProfile>,
    withdrawals: List<WithdrawalRequest>,
    announcements: List<Announcement>,
    repository: FirestoreRepository,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Withdrawals", "Users", "Announcements")

    var rejectWithdrawalTarget by remember { mutableStateOf<WithdrawalRequest?>(null) }
    var payWithdrawalTarget by remember { mutableStateOf<WithdrawalRequest?>(null) }
    var showNewAnnouncementDialog by remember { mutableStateOf(false) }
    var userSearchQuery by remember { mutableStateOf("") }

    NeonGalaxyBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyan
                            )
                        }
                        Text(
                            text = "ADMIN CONSOLE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = NeonSurface,
                        contentColor = NeonCyan,
                        edgePadding = 0.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = NeonCyan
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (selectedTab == index) NeonCyan else TextSecondary,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // TAB 0: OVERVIEW
                if (selectedTab == 0) {
                    val pendingCount = withdrawals.count { it.status == "PENDING" }
                    val approvedCount = withdrawals.count { it.status == "APPROVED" }
                    val paidCount = withdrawals.count { it.status == "PAID" }
                    val rejectedCount = withdrawals.count { it.status == "REJECTED" }
                    val totalGames = users.sumOf { it.gamesPlayed }
                    val totalLines = users.sumOf { it.linesCleared }

                    item {
                        Text(
                            text = "Platform Analytics",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminMetricCard("Total Users", "${users.size}", NeonCyan, Modifier.weight(1f))
                            AdminMetricCard("Total Games", "$totalGames", NeonPurple, Modifier.weight(1f))
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminMetricCard("Lines Cleared", "$totalLines", NeonGreen, Modifier.weight(1f))
                            AdminMetricCard("Pending Payouts", "$pendingCount", NeonAmber, Modifier.weight(1f))
                        }
                    }

                    item {
                        NeonCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan) {
                            Column {
                                Text("Withdrawal Breakdown", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pending Review: $pendingCount", color = NeonAmber)
                                    Text("Approved: $approvedCount", color = NeonCyan)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Paid Out: $paidCount", color = NeonGreen)
                                    Text("Rejected (Reversed): $rejectedCount", color = DangerNeon)
                                }
                            }
                        }
                    }
                }

                // TAB 1: WITHDRAWALS MANAGEMENT
                if (selectedTab == 1) {
                    if (withdrawals.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No withdrawal requests on file.", color = TextSecondary)
                            }
                        }
                    } else {
                        items(withdrawals, key = { it.id }) { req ->
                            NeonCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = when (req.status) {
                                    "PAID" -> NeonGreen
                                    "APPROVED" -> NeonCyan
                                    "REJECTED" -> DangerNeon
                                    else -> NeonAmber
                                }
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = String.format(Locale.US, "$%.2f %s", req.amount, req.currency),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "User: ${req.userEmail} • ${req.payoutMethod}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "Details: ${req.payoutDetails}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                            Text(
                                                text = "Ref: ${req.reference}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeonCyan
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (req.status) {
                                                        "PAID" -> NeonGreen.copy(alpha = 0.2f)
                                                        "APPROVED" -> NeonCyan.copy(alpha = 0.2f)
                                                        "REJECTED" -> DangerNeon.copy(alpha = 0.2f)
                                                        else -> NeonAmber.copy(alpha = 0.2f)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = req.status,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = when (req.status) {
                                                    "PAID" -> NeonGreen
                                                    "APPROVED" -> NeonCyan
                                                    "REJECTED" -> DangerNeon
                                                    else -> NeonAmber
                                                }
                                            )
                                        }
                                    }

                                    // Action Buttons for Pending or Approved
                                    if (req.status == "PENDING") {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            NeonButton(
                                                text = "Approve",
                                                glowColor = NeonGreen,
                                                onClick = {
                                                    scope.launch {
                                                        repository.approveWithdrawal(req.id)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )

                                            NeonOutlinedButton(
                                                text = "Reject & Refund",
                                                glowColor = DangerNeon,
                                                onClick = { rejectWithdrawalTarget = req },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    } else if (req.status == "APPROVED") {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        NeonButton(
                                            text = "Mark as Paid",
                                            glowColor = NeonCyan,
                                            onClick = { payWithdrawalTarget = req },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: USER MANAGEMENT
                if (selectedTab == 2) {
                    item {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search by name, email, or ID...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0x44334166),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    val filteredUsers = users.filter {
                        it.username.contains(userSearchQuery, ignoreCase = true) ||
                        it.email.contains(userSearchQuery, ignoreCase = true) ||
                        it.userId.contains(userSearchQuery, ignoreCase = true)
                    }

                    items(filteredUsers, key = { it.userId }) { u ->
                        NeonCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (u.isSuspended) DangerNeon else NeonCyan
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = u.username.ifBlank { "Player" },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(u.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Text("Best Score: ${u.bestScore} • Games: ${u.gamesPlayed}", style = MaterialTheme.typography.labelSmall, color = NeonGold)
                                        Text("Balance: $${String.format(Locale.US, "%.2f", u.eligibleBalance)} • Coins: ${u.gameCoins}", style = MaterialTheme.typography.labelSmall, color = NeonGreen)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (u.isAdmin) {
                                            Text("ADMIN", color = NeonCyan, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                        if (u.isSuspended) {
                                            Text("SUSPENDED", color = DangerNeon, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NeonOutlinedButton(
                                        text = if (u.isSuspended) "Reactivate" else "Suspend",
                                        glowColor = if (u.isSuspended) NeonGreen else DangerNeon,
                                        onClick = {
                                            scope.launch {
                                                repository.toggleUserSuspension(u.userId, !u.isSuspended)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    NeonOutlinedButton(
                                        text = if (u.isAdmin) "Revoke Admin" else "Make Admin",
                                        glowColor = NeonPurple,
                                        onClick = {
                                            scope.launch {
                                                repository.makeUserAdmin(u.userId, !u.isAdmin)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 3: ANNOUNCEMENTS
                if (selectedTab == 3) {
                    item {
                        NeonButton(
                            text = "+ New Announcement",
                            glowColor = NeonCyan,
                            onClick = { showNewAnnouncementDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(announcements, key = { it.id }) { ann ->
                        NeonCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan) {
                            Column {
                                Text(ann.title, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(ann.content, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // Rejection Dialog with Reason & Atomic Reversal
    rejectWithdrawalTarget?.let { target ->
        var reasonText by remember { mutableStateOf("Invalid payment account details") }
        AlertDialog(
            onDismissRequest = { rejectWithdrawalTarget = null },
            title = {
                Text("REJECT WITHDRAWAL", color = DangerNeon, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Rejecting this request will restore $${String.format(Locale.US, "%.2f", target.amount)} back to user's available balance and write an auditable reversal record.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                NeonButton(
                    text = "Confirm Rejection",
                    glowColor = DangerNeon,
                    onClick = {
                        scope.launch {
                            repository.rejectWithdrawal(target.id, reasonText)
                            rejectWithdrawalTarget = null
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { rejectWithdrawalTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }

    // Mark Paid Dialog
    payWithdrawalTarget?.let { target ->
        var paymentRefText by remember { mutableStateOf("PAY-" + target.reference) }
        AlertDialog(
            onDismissRequest = { payWithdrawalTarget = null },
            title = {
                Text("CONFIRM PAYMENT", color = NeonGreen, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Mark withdrawal of $${String.format(Locale.US, "%.2f", target.amount)} to ${target.userEmail} as PAID.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = paymentRefText,
                        onValueChange = { paymentRefText = it },
                        label = { Text("Payment / Bank / Tx Reference") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                NeonButton(
                    text = "Confirm Paid",
                    glowColor = NeonGreen,
                    onClick = {
                        scope.launch {
                            repository.markWithdrawalPaid(target.id, paymentRefText)
                            payWithdrawalTarget = null
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { payWithdrawalTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }

    // New Announcement Dialog
    if (showNewAnnouncementDialog) {
        var titleText by remember { mutableStateOf("") }
        var contentText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewAnnouncementDialog = false },
            title = {
                Text("PUBLISH ANNOUNCEMENT", color = NeonCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                NeonButton(
                    text = "Publish",
                    glowColor = NeonCyan,
                    enabled = titleText.isNotBlank() && contentText.isNotBlank(),
                    onClick = {
                        scope.launch {
                            repository.publishAnnouncement(titleText, contentText, "UPDATE")
                            showNewAnnouncementDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showNewAnnouncementDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }
}

@Composable
private fun AdminMetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NeonSurface)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = color)
        }
    }
}
