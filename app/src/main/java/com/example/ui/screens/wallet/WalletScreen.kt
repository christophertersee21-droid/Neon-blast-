package com.example.ui.screens.wallet

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBanner
import com.example.ads.AdMobManager
import com.example.data.model.AppConfig
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val NIGERIAN_BANKS = listOf(
    "Access Bank",
    "GTBank (Guaranty Trust)",
    "Zenith Bank",
    "First Bank of Nigeria",
    "United Bank for Africa (UBA)",
    "Kuda Bank",
    "OPay",
    "PalmPay",
    "Moniepoint MFB",
    "Stanbic IBTC Bank",
    "Fidelity Bank",
    "Union Bank",
    "Sterling Bank",
    "Wema Bank / ALAT"
)

@Composable
fun WalletScreen(
    user: UserProfile,
    withdrawals: List<WithdrawalRequest>,
    repository: FirestoreRepository,
    onViewTransactionsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val config by repository.observeAppConfig().collectAsState(initial = AppConfig())

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var isWatchingAd by remember { mutableStateOf(false) }

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
                        color = Color.White
                    )
                    Text(
                        text = "Nigeria 🇳🇬 Real Cash Payouts",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonGreen
                    )
                }

                IconButton(
                    onClick = onViewTransactionsClicked,
                    modifier = Modifier.testTag("ledger_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Transactions Ledger",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Primary Balance Card (Naira)
        item {
            NeonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_card"),
                borderColor = NeonGreen
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE WITHDRAWABLE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NGN (₦)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₦${String.format(Locale.US, "%,.2f", user.eligibleBalance)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = NeonGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Min withdrawal: ₦${config.minWithdrawalNgn.toInt()} Naira • Direct Nigerian Bank Transfer",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeonButton(
                            text = "Withdraw ₦",
                            glowColor = NeonGreen,
                            onClick = { showWithdrawDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("withdraw_button")
                        )

                        NeonOutlinedButton(
                            text = "Ledger",
                            glowColor = NeonCyan,
                            onClick = onViewTransactionsClicked,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            }
        }

        // AdMob Rewarded Video Card
        if (config.adMobEnabled) {
            item {
                NeonCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonGold
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Earn Cash with Video Ad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🎥", fontSize = 14.sp)
                            }
                            Text(
                                text = "+${config.rewardedAdCoins} Coins & +₦${config.rewardedAdNgn} Naira per ad",
                                color = NeonGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        NeonButton(
                            text = if (isWatchingAd) "Loading..." else "Watch Ad",
                            glowColor = NeonGold,
                            onClick = {
                                if (activity != null && !isWatchingAd) {
                                    isWatchingAd = true
                                    AdMobManager.showRewarded(
                                        activity = activity,
                                        adUnitId = config.adMobRewardedId,
                                        onUserEarnedReward = {
                                            isWatchingAd = false
                                            scope.launch {
                                                repository.rewardUserForWatchingAd(user.userId)
                                                Toast.makeText(context, "🎉 Rewarded! ₦${config.rewardedAdNgn} added to wallet!", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        onFailed = { err ->
                                            isWatchingAd = false
                                            Toast.makeText(context, "Ad not ready yet. Please try again soon.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // Secondary Balances: Game Coins, Pending, and Total Paid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Game Coins
                NeonCard(
                    modifier = Modifier.weight(1f),
                    borderColor = NeonGold.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("GAME COINS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${user.gameCoins}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = NeonGold
                        )
                        Text("For game revives", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                // Pending Withdrawals
                NeonCard(
                    modifier = Modifier.weight(1f),
                    borderColor = NeonAmber.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("IN PROCESSING", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₦${String.format(Locale.US, "%,.2f", user.pendingWithdrawal)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = NeonAmber
                        )
                        Text("Awaiting Admin", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }

        // Total Withdrawn Card
        item {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL PAID OUT", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = "₦${String.format(Locale.US, "%,.2f", user.totalWithdrawn)} Naira",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                    }
                    Text("Instant NUBAN Payouts 🏦", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Withdrawal History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Withdrawal Requests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                TextButton(onClick = onViewTransactionsClicked) {
                    Text("Audit Ledger", color = NeonCyan)
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
                    Text("No withdrawal requests yet. Play or watch ads to withdraw!", color = TextSecondary)
                }
            }
        } else {
            items(withdrawals) { req ->
                NeonCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = when (req.status) {
                        "APPROVED" -> NeonGreen
                        "REJECTED" -> DangerNeon
                        else -> NeonAmber
                    }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₦${req.amount} NGN",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (req.status) {
                                            "APPROVED" -> NeonGreen.copy(alpha = 0.2f)
                                            "REJECTED" -> DangerNeon.copy(alpha = 0.2f)
                                            else -> NeonAmber.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = req.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (req.status) {
                                        "APPROVED" -> NeonGreen
                                        "REJECTED" -> DangerNeon
                                        else -> NeonAmber
                                    }
                                )
                            }
                        }

                        Text(text = "${req.bankName} • ${req.accountNumber} (${req.accountName})", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "Ref: ${req.reference}", color = TextSecondary, fontSize = 11.sp)

                        if (req.status == "REJECTED" && req.rejectionReason.isNotBlank()) {
                            Text(text = "Rejected Reason: ${req.rejectionReason} (Refunded)", color = DangerNeon, fontSize = 11.sp)
                        } else if (req.status == "APPROVED" && req.paymentReference.isNotBlank()) {
                            Text(text = "Payment Ref: ${req.paymentReference}", color = NeonGreen, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // AdMob Banner
        item {
            AdMobBanner(
                adUnitId = config.adMobBannerId,
                isEnabled = config.adMobEnabled
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Nigerian Withdrawal Request Dialog
    if (showWithdrawDialog) {
        var withdrawAmountText by remember { mutableStateOf("${config.minWithdrawalNgn.toInt()}") }
        var selectedBank by remember { mutableStateOf(NIGERIAN_BANKS.first()) }
        var accountNumberText by remember { mutableStateOf("") }
        var accountNameText by remember { mutableStateOf(user.username) }
        var isSubmitting by remember { mutableStateOf(false) }
        var dialogError by remember { mutableStateOf<String?>(null) }
        var showBankPicker by remember { mutableStateOf(false) }

        val minAmount = config.minWithdrawalNgn
        val presets = listOf(100.0, 250.0, 500.0, 1000.0, 5000.0)

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showWithdrawDialog = false },
            title = {
                Text("WITHDRAW NAIRA (₦)", color = NeonGreen, fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Balance: ₦${String.format(Locale.US, "%,.2f", user.eligibleBalance)} (Min: ₦${minAmount.toInt()})",
                        color = Color.White,
                        fontSize = 13.sp
                    )

                    // Presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presets) { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonSurface)
                                    .border(1.dp, if (withdrawAmountText == "${p.toInt()}") NeonGreen else Color(0x33334166), RoundedCornerShape(8.dp))
                                    .clickable { withdrawAmountText = "${p.toInt()}" }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("₦${p.toInt()}", color = if (withdrawAmountText == "${p.toInt()}") NeonGreen else TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

                    // Amount Field
                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it },
                        label = { Text("Amount in Naira (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bank Selection
                    Column {
                        Text("Select Nigerian Bank:", color = TextSecondary, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonSurface)
                                .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                                .clickable { showBankPicker = !showBankPicker }
                                .padding(12.dp)
                        ) {
                            Text(selectedBank, color = Color.White, fontSize = 13.sp)
                        }

                        if (showBankPicker) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonBackground)
                                    .padding(4.dp)
                            ) {
                                LazyColumn {
                                    items(NIGERIAN_BANKS) { bank ->
                                        Text(
                                            text = bank,
                                            color = if (bank == selectedBank) NeonGreen else Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedBank = bank
                                                    showBankPicker = false
                                                }
                                                .padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 10-Digit Account Number
                    OutlinedTextField(
                        value = accountNumberText,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) accountNumberText = it },
                        label = { Text("10-Digit NUBAN Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Account Name
                    OutlinedTextField(
                        value = accountNameText,
                        onValueChange = { accountNameText = it },
                        label = { Text("Account Holder Full Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    dialogError?.let { err ->
                        Text(text = err, color = DangerNeon, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                NeonButton(
                    text = if (isSubmitting) "Submitting..." else "Confirm Withdrawal",
                    glowColor = NeonGreen,
                    onClick = {
                        val amount = withdrawAmountText.toDoubleOrNull() ?: 0.0
                        if (amount < minAmount) {
                            dialogError = "Minimum withdrawal is ₦${minAmount.toInt()} Naira"
                            return@NeonButton
                        }
                        if (amount > user.eligibleBalance) {
                            dialogError = "Insufficient balance (₦${user.eligibleBalance} available)"
                            return@NeonButton
                        }
                        if (accountNumberText.length != 10) {
                            dialogError = "Account number must be exactly 10 digits"
                            return@NeonButton
                        }
                        if (accountNameText.isBlank()) {
                            dialogError = "Please enter account name"
                            return@NeonButton
                        }

                        isSubmitting = true
                        dialogError = null
                        scope.launch {
                            val res = repository.requestWithdrawal(
                                userId = user.userId,
                                userEmail = user.email,
                                amount = amount,
                                bankName = selectedBank,
                                accountNumber = accountNumberText,
                                accountName = accountNameText
                            )
                            isSubmitting = false
                            if (res.isSuccess) {
                                showWithdrawDialog = false
                            } else {
                                dialogError = res.exceptionOrNull()?.message ?: "Withdrawal request failed"
                            }
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }
}
