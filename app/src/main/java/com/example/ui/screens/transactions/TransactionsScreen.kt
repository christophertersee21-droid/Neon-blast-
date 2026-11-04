package com.example.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionItem
import com.example.ui.components.NeonCard
import com.example.ui.components.NeonGalaxyBackground
import com.example.ui.theme.DangerNeon
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonSurface
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    transactions: List<TransactionItem>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All", "Credits", "Withdrawals", "Pending", "Approved", "Rejected")
    var selectedTxForDetails by remember { mutableStateOf<TransactionItem?>(null) }

    val filteredList = transactions.filter { tx ->
        when (selectedFilterIndex) {
            0 -> true
            1 -> tx.amount > 0
            2 -> tx.type.contains("WITHDRAWAL", ignoreCase = true)
            3 -> tx.status.equals("PENDING", ignoreCase = true)
            4 -> tx.status.equals("APPROVED", ignoreCase = true) || tx.status.equals("COMPLETED", ignoreCase = true) || tx.status.equals("PAID", ignoreCase = true)
            5 -> tx.status.equals("REJECTED", ignoreCase = true)
            else -> true
        }
    }

    NeonGalaxyBackground {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "AUDITABLE LEDGER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedFilterIndex,
                    containerColor = NeonSurface,
                    contentColor = NeonCyan,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedFilterIndex]),
                            color = NeonCyan
                        )
                    }
                ) {
                    filterTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedFilterIndex == index) NeonCyan else TextSecondary,
                                    fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No ledger transactions found.", color = TextSecondary)
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { tx ->
                    val isPositive = tx.amount >= 0
                    val dateFormatted = try {
                        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(tx.createdAt))
                    } catch (_: Exception) {
                        "Just now"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonSurface)
                            .border(
                                1.dp,
                                if (isPositive) NeonGreen.copy(alpha = 0.3f) else NeonAmber.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTxForDetails = tx }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.description.ifBlank { tx.type },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                Text(
                                    text = "$dateFormatted • Ref: ${tx.reference.take(12)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "%s₦%.2f", if (isPositive) "+" else "", tx.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (isPositive) NeonGreen else DangerNeon
                                )
                                Text(
                                    text = tx.status,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (tx.status) {
                                        "COMPLETED", "APPROVED", "PAID" -> NeonGreen
                                        "REJECTED" -> DangerNeon
                                        else -> NeonAmber
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

    // Transaction Details Dialog
    selectedTxForDetails?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTxForDetails = null },
            title = {
                Text("TRANSACTION DETAILS", color = NeonCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    DetailRow("ID", tx.id)
                    DetailRow("Reference", tx.reference)
                    DetailRow("Type", tx.type)
                    DetailRow("Amount", String.format(Locale.US, "₦%.2f %s", tx.amount, tx.currency))
                    DetailRow("Balance After", String.format(Locale.US, "₦%.2f", tx.balanceAfter))
                    DetailRow("Status", tx.status)
                    DetailRow("Description", tx.description)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTxForDetails = null }) {
                    Text("Close", color = NeonCyan)
                }
            },
            containerColor = NeonSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
    }
}
