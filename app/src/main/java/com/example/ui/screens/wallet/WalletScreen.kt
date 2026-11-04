package com.example.ui.screens.wallet

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.theme.DangerNeon
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBackground
import com.example.ui.theme.NeonCardBorder
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
fun WalletScreen(
    user: UserProfile,
    withdrawals: List<WithdrawalRequest>,
    repository: FirestoreRepository,
    onViewTransactionsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "REWARD WALLET",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = NeonCyan
                    )
                    Text(
                        text = "Separate game and legally eligible withdrawable balances",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onViewTransactionsClicked,
                    modifier = Modifier.testTag("transactions_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Transactions",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Eligible Withdrawable Balance Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0F2B20),
                                Color(0xFF061A13),
                                Color(0xFF0D1E2A)
                            )
                        )
                    )
                    .border(1.5.dp, NeonGreen, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ELIGIBLE REWARDS WALLET",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen
                        )
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Rewards",
                            tint = NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = String.format(Locale.US, "$%.2f", user.eligibleBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pending Approval", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "$%.2f", user.pendingWithdrawal),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeonAmber
                            )
                        }
                        Column {
                            Text("Total Paid Out", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "$%.2f", user.totalWithdrawn),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    NeonButton(
                        text = "Request Withdrawal",
                        glowColor = NeonGreen,
                        enabled = user.eligibleBalance >= 5.0,
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_withdrawal_button")
                    )
                }
            }
        }

        // Game Wallet Card
        item {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "GAME WALLET (NON-TRANSFERABLE)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙 ", fontSize = 22.sp)
                            Text(
                                text = "${user.gameCoins} Coins",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Used for Second Chance revival, in-game boosts, and specials.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = NeonGold,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Notice Box
        item {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.3f)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notice",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All wallet calculations and ledger entries are enforced securely on the backend. When a withdrawal is requested, funds are reserved until approved or restored upon rejection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Recent Withdrawal Requests
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent Withdrawal Requests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                TextButton(onClick = onViewTransactionsClicked) {
                    Text("Ledger History", color = NeonCyan)
                }
            }
        }

        if (withdrawals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No withdrawal requests yet.", color = TextSecondary)
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
                                    text = "${req.payoutMethod} • ${req.reference}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            // Status badge
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

                        if (req.rejectionReason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Reason: ${req.rejectionReason} (Funds Restored)",
                                style = MaterialTheme.typography.labelSmall,
                                color = DangerNeon
                            )
                        }

                        if (req.paymentReference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tx Ref: ${req.paymentReference}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonGreen
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

    // Withdrawal Request Dialog
    if (showWithdrawDialog) {
        var withdrawAmountText by remember { mutableStateOf("5.00") }
        var selectedPayoutMethod by remember { mutableStateOf("PayPal") }
        var payoutDetailsText by remember { mutableStateOf(user.email) }
        var isSubmitting by remember { mutableStateOf(false) }
        var dialogError by remember { mutableStateOf<String?>(null) }
        val payoutOptions = listOf("PayPal", "Bank Transfer", "Crypto (USDT)", "Gift Card")

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showWithdrawDialog = false },
            title = {
                Text("SUBMIT WITHDRAWAL", color = NeonGreen, fontWeight = FontWeight.Black)
            },
            text = {
                Column {
                    Text(
                        text = "Available: ${String.format(Locale.US, "$%.2f", user.eligibleBalance)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Field
                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it },
                        label = { Text("Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = NeonCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Payout Method:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        payoutOptions.take(2).forEach { method ->
                            val isSel = selectedPayoutMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) NeonGreen.copy(alpha = 0.25f) else NeonSurface)
                                    .border(1.dp, if (isSel) NeonGreen else Color(0x33334166), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { selectedPayoutMethod = method }) {
                                    Text(method, color = if (isSel) NeonGreen else TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Payout Details
                    OutlinedTextField(
                        value = payoutDetailsText,
                        onValueChange = { payoutDetailsText = it },
                        label = { Text("Payout Details (Email / Wallet / IBAN)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = NeonCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (dialogError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(dialogError ?: "", color = DangerNeon, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                NeonButton(
                    text = "Submit Request",
                    glowColor = NeonGreen,
                    enabled = !isSubmitting,
                    onClick = {
                        val amount = withdrawAmountText.toDoubleOrNull()
                        if (amount == null || amount < 5.0) {
                            dialogError = "Minimum withdrawal is $5.00."
                            return@NeonButton
                        }
                        if (amount > user.eligibleBalance) {
                            dialogError = "Insufficient eligible balance."
                            return@NeonButton
                        }
                        if (payoutDetailsText.isBlank()) {
                            dialogError = "Please enter payout details."
                            return@NeonButton
                        }

                        isSubmitting = true
                        dialogError = null
                        scope.launch {
                            val res = repository.requestWithdrawal(
                                amount = amount,
                                payoutMethod = selectedPayoutMethod,
                                payoutDetails = payoutDetailsText,
                                userEmail = user.email
                            )
                            isSubmitting = false
                            if (res.isSuccess) {
                                showWithdrawDialog = false
                            } else {
                                dialogError = res.exceptionOrNull()?.message ?: "Failed to submit."
                            }
                        }
                    }
                )
            },
            dismissButton = {
                NeonOutlinedButton(
                    text = "Cancel",
                    glowColor = TextSecondary,
                    onClick = { showWithdrawDialog = false }
                )
            },
            containerColor = NeonSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
