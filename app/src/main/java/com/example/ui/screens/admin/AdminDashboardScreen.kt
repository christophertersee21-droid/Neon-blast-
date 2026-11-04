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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.AppConfig
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.data.model.isUserAdminEmail
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
    val currentUserEmail = Firebase.auth.currentUser?.email
    val isAuthorizedAdmin = isUserAdminEmail(currentUserEmail)

    val remoteConfig by repository.observeAppConfig().collectAsState(initial = AppConfig())
    val tasksList by repository.observeTasks().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Remote Config", "Withdrawals", "Users", "Missions", "Announcements")

    // Modals & form state
    var showSaveConfigSuccessDialog by remember { mutableStateOf(false) }
    var rejectWithdrawalTarget by remember { mutableStateOf<WithdrawalRequest?>(null) }
    var payWithdrawalTarget by remember { mutableStateOf<WithdrawalRequest?>(null) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showNewAnnouncementDialog by remember { mutableStateOf(false) }
    var adjustUserTarget by remember { mutableStateOf<UserProfile?>(null) }

    var userSearchQuery by remember { mutableStateOf("") }

    // Remote Config editable state
    var minWithdrawalText by remember { mutableStateOf("100.0") }
    var dailyRewardCoinsText by remember { mutableStateOf("100") }
    var adMobEnabled by remember { mutableStateOf(true) }
    var adMobBannerIdText by remember { mutableStateOf("") }
    var adMobInterstitialIdText by remember { mutableStateOf("") }
    var adMobRewardedIdText by remember { mutableStateOf("") }
    var rewardedAdCoinsText by remember { mutableStateOf("50") }
    var rewardedAdNgnText by remember { mutableStateOf("5.0") }
    var nigeriaOnlyEnabled by remember { mutableStateOf(true) }
    var maintenanceMode by remember { mutableStateOf(false) }
    var maintenanceMessageText by remember { mutableStateOf("") }
    var announcementBannerText by remember { mutableStateOf("") }
    var secondChanceCostCoinsText by remember { mutableStateOf("30") }
    var secondChanceAdReviveEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(remoteConfig) {
        minWithdrawalText = remoteConfig.minWithdrawalNgn.toString()
        dailyRewardCoinsText = remoteConfig.dailyRewardCoins.toString()
        adMobEnabled = remoteConfig.adMobEnabled
        adMobBannerIdText = remoteConfig.adMobBannerId
        adMobInterstitialIdText = remoteConfig.adMobInterstitialId
        adMobRewardedIdText = remoteConfig.adMobRewardedId
        rewardedAdCoinsText = remoteConfig.rewardedAdCoins.toString()
        rewardedAdNgnText = remoteConfig.rewardedAdNgn.toString()
        nigeriaOnlyEnabled = remoteConfig.nigeriaOnlyEnabled
        maintenanceMode = remoteConfig.maintenanceMode
        maintenanceMessageText = remoteConfig.maintenanceMessage
        announcementBannerText = remoteConfig.announcementBanner
        secondChanceCostCoinsText = remoteConfig.secondChanceCoinCost.toString()
        secondChanceAdReviveEnabled = remoteConfig.secondChanceAdReviveEnabled
    }

    NeonGalaxyBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isAuthorizedAdmin) {
                // Access Denied screen for non-admins
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unauthorized",
                        tint = DangerNeon,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Access Restricted",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DangerNeon
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This console is strictly limited to authorized administrators (chrstphrnch@gmail.com, christopherenoch767@gmail.com, Christophertersee21@gmail.com).",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    NeonButton(
                        text = "Back to Game",
                        onClick = onBackClicked,
                        glowColor = NeonCyan
                    )
                }
            } else {
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
                                text = "ADMIN MASTER CONSOLE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NeonGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    item {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = NeonSurface,
                            contentColor = NeonCyan,
                            edgePadding = 0.dp,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = NeonGreen
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
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedTab == index) NeonGreen else TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // TAB 0: REMOTE CONFIGURATION (NO CODING NEEDED)
                    if (selectedTab == 0) {
                        item {
                            NeonCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGreen) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = NeonGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Live Remote App Controls",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonGreen
                                        )
                                    }
                                    Text(
                                        text = "Any changes saved here instantly update all live app instances via Realtime Database without recompilation.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Minimum Withdrawal
                                    OutlinedTextField(
                                        value = minWithdrawalText,
                                        onValueChange = { minWithdrawalText = it },
                                        label = { Text("Minimum Withdrawal (₦ Naira)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonGreen,
                                            unfocusedBorderColor = NeonSurface,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Daily Bonus Coins
                                    OutlinedTextField(
                                        value = dailyRewardCoinsText,
                                        onValueChange = { dailyRewardCoinsText = it },
                                        label = { Text("Daily Login Bonus (Game Coins)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonGreen,
                                            unfocusedBorderColor = NeonSurface,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Second Chance Revive Cost
                                    OutlinedTextField(
                                        value = secondChanceCostCoinsText,
                                        onValueChange = { secondChanceCostCoinsText = it },
                                        label = { Text("Game Revive Cost (Coins)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonGreen,
                                            unfocusedBorderColor = NeonSurface,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // In-App Global Announcement
                                    OutlinedTextField(
                                        value = announcementBannerText,
                                        onValueChange = { announcementBannerText = it },
                                        label = { Text("Global Announcement Banner") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonGreen,
                                            unfocusedBorderColor = NeonSurface,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Nigeria Only Access Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Nigeria Only Access 🇳🇬", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Restrict app access to players within Nigeria", color = TextSecondary, fontSize = 11.sp)
                                        }
                                        Switch(
                                            checked = nigeriaOnlyEnabled,
                                            onCheckedChange = { nigeriaOnlyEnabled = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonSurface)
                                        )
                                    }

                                    // Maintenance Mode Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Maintenance Mode 🛠️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Displays maintenance screen to players", color = TextSecondary, fontSize = 11.sp)
                                        }
                                        Switch(
                                            checked = maintenanceMode,
                                            onCheckedChange = { maintenanceMode = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = DangerNeon, checkedTrackColor = NeonSurface)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            NeonCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Google AdMob Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                                        Switch(
                                            checked = adMobEnabled,
                                            onCheckedChange = { adMobEnabled = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonSurface)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = rewardedAdCoinsText,
                                        onValueChange = { rewardedAdCoinsText = it },
                                        label = { Text("Rewarded Ad Coins Reward") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = rewardedAdNgnText,
                                        onValueChange = { rewardedAdNgnText = it },
                                        label = { Text("Rewarded Ad Naira (₦) Reward") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Free Revive via Rewarded Ad", color = Color.White, fontSize = 13.sp)
                                        Switch(
                                            checked = secondChanceAdReviveEnabled,
                                            onCheckedChange = { secondChanceAdReviveEnabled = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = adMobBannerIdText,
                                        onValueChange = { adMobBannerIdText = it },
                                        label = { Text("AdMob Banner Unit ID") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = adMobInterstitialIdText,
                                        onValueChange = { adMobInterstitialIdText = it },
                                        label = { Text("AdMob Interstitial Unit ID") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = adMobRewardedIdText,
                                        onValueChange = { adMobRewardedIdText = it },
                                        label = { Text("AdMob Rewarded Unit ID") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        item {
                            NeonButton(
                                text = "Save Remote Configuration",
                                glowColor = NeonGreen,
                                onClick = {
                                    val updated = remoteConfig.copy(
                                        minWithdrawalNgn = minWithdrawalText.toDoubleOrNull() ?: 100.0,
                                        dailyRewardCoins = dailyRewardCoinsText.toLongOrNull() ?: 100L,
                                        adMobEnabled = adMobEnabled,
                                        adMobBannerId = adMobBannerIdText.ifBlank { remoteConfig.adMobBannerId },
                                        adMobInterstitialId = adMobInterstitialIdText.ifBlank { remoteConfig.adMobInterstitialId },
                                        adMobRewardedId = adMobRewardedIdText.ifBlank { remoteConfig.adMobRewardedId },
                                        rewardedAdCoins = rewardedAdCoinsText.toLongOrNull() ?: 50L,
                                        rewardedAdNgn = rewardedAdNgnText.toDoubleOrNull() ?: 5.0,
                                        nigeriaOnlyEnabled = nigeriaOnlyEnabled,
                                        maintenanceMode = maintenanceMode,
                                        maintenanceMessage = maintenanceMessageText,
                                        announcementBanner = announcementBannerText,
                                        secondChanceCoinCost = secondChanceCostCoinsText.toLongOrNull() ?: 30L,
                                        secondChanceAdReviveEnabled = secondChanceAdReviveEnabled
                                    )
                                    scope.launch {
                                        repository.updateAppConfig(updated)
                                        showSaveConfigSuccessDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_config_button")
                            )
                        }
                    }

                    // TAB 1: WITHDRAWALS
                    if (selectedTab == 1) {
                        item {
                            val pendingCount = withdrawals.count { it.status == "PENDING" }
                            Text(
                                text = "Withdrawal Requests ($pendingCount pending)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber
                            )
                        }

                        if (withdrawals.isEmpty()) {
                            item {
                                Text("No withdrawal requests found.", color = TextSecondary, fontSize = 14.sp)
                            }
                        } else {
                            items(withdrawals) { w ->
                                NeonCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    borderColor = when (w.status) {
                                        "PENDING" -> NeonAmber
                                        "APPROVED" -> NeonGreen
                                        else -> DangerNeon
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "₦${w.amount} NGN",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonGold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (w.status) {
                                                            "PENDING" -> NeonAmber.copy(alpha = 0.2f)
                                                            "APPROVED" -> NeonGreen.copy(alpha = 0.2f)
                                                            else -> DangerNeon.copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = w.status,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (w.status) {
                                                        "PENDING" -> NeonAmber
                                                        "APPROVED" -> NeonGreen
                                                        else -> DangerNeon
                                                    }
                                                )
                                            }
                                        }

                                        Text(text = "User: ${w.userName} (${w.userEmail})", color = Color.White, fontSize = 12.sp)
                                        Text(text = "Bank: ${w.bankName}", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "Account: ${w.accountNumber} • ${w.accountName}", color = TextSecondary, fontSize = 12.sp)
                                        Text(text = "Ref: ${w.reference}", color = TextSecondary, fontSize = 11.sp)

                                        if (w.status == "PENDING") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                NeonButton(
                                                    text = "Approve & Pay",
                                                    glowColor = NeonGreen,
                                                    onClick = { payWithdrawalTarget = w },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                NeonOutlinedButton(
                                                    text = "Reject & Refund",
                                                    glowColor = DangerNeon,
                                                    onClick = { rejectWithdrawalTarget = w },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        } else if (w.status == "APPROVED") {
                                            Text(text = "Paid Ref: ${w.paymentReference}", color = NeonGreen, fontSize = 11.sp)
                                        } else if (w.status == "REJECTED") {
                                            Text(text = "Reason: ${w.rejectionReason}", color = DangerNeon, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: USERS
                    if (selectedTab == 2) {
                        item {
                            OutlinedTextField(
                                value = userSearchQuery,
                                onValueChange = { userSearchQuery = it },
                                placeholder = { Text("Search by email or username...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = NeonSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        val filteredUsers = users.filter {
                            it.email.contains(userSearchQuery, ignoreCase = true) ||
                            it.username.contains(userSearchQuery, ignoreCase = true)
                        }

                        items(filteredUsers) { u ->
                            NeonCard(modifier = Modifier.fillMaxWidth(), borderColor = if (u.hasAdminPrivileges()) NeonGold else NeonSurface) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = u.username,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (u.hasAdminPrivileges()) {
                                            Text("ADMIN ⭐", color = NeonGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(text = u.email, color = TextSecondary, fontSize = 12.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Balance: ₦${u.eligibleBalance}", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "Coins: ${u.gameCoins}", color = NeonGold, fontSize = 12.sp)
                                        Text(text = "Best: ${u.bestScore}", color = NeonCyan, fontSize = 12.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { adjustUserTarget = u }) {
                                            Text("Adjust Balance", color = NeonCyan, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 3: MISSIONS
                    if (selectedTab == 3) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Active Game Tasks (${tasksList.size})", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                NeonButton(
                                    text = "Add Task",
                                    glowColor = NeonPurple,
                                    onClick = { showNewTaskDialog = true }
                                )
                            }
                        }

                        items(tasksList) { task ->
                            NeonCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPurple) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(task.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(task.description, color = TextSecondary, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Reward: +${task.rewardCoins} coins • +₦${task.rewardEligible} Naira (Target: ${task.target})",
                                            color = NeonGreen,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(onClick = {
                                        scope.launch { repository.deleteTask(task.id) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerNeon)
                                    }
                                }
                            }
                        }
                    }

                    // TAB 4: ANNOUNCEMENTS
                    if (selectedTab == 4) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("In-App Announcements", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                NeonButton(
                                    text = "Post New",
                                    glowColor = NeonCyan,
                                    onClick = { showNewAnnouncementDialog = true }
                                )
                            }
                        }

                        items(announcements) { a ->
                            NeonCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(a.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(a.content, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // Success dialog for remote config save
    if (showSaveConfigSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfigSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSaveConfigSuccessDialog = false }) {
                    Text("OK", color = NeonGreen)
                }
            },
            title = { Text("Remote Configuration Saved! 🚀", color = NeonGreen, fontWeight = FontWeight.Bold) },
            text = { Text("The changes have been written to Firebase Realtime Database. All live client apps have been synchronized automatically.", color = Color.White) },
            containerColor = NeonSurface
        )
    }

    // Modal: Approve Withdrawal
    payWithdrawalTarget?.let { target ->
        var paymentRef by remember { mutableStateOf("NIBSS-TRX-${System.currentTimeMillis().toString().takeLast(6)}") }
        AlertDialog(
            onDismissRequest = { payWithdrawalTarget = null },
            title = { Text("Approve Withdrawal of ₦${target.amount}", color = NeonGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Recipient: ${target.accountName} (${target.bankName} - ${target.accountNumber})", color = Color.White, fontSize = 13.sp)
                    OutlinedTextField(
                        value = paymentRef,
                        onValueChange = { paymentRef = it },
                        label = { Text("Bank Payment Ref / Session ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.approveWithdrawal(target.id, paymentRef)
                        payWithdrawalTarget = null
                    }
                }) {
                    Text("Confirm Payment", color = NeonGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { payWithdrawalTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }

    // Modal: Reject Withdrawal
    rejectWithdrawalTarget?.let { target ->
        var rejectReason by remember { mutableStateOf("Account name does not match KYC") }
        AlertDialog(
            onDismissRequest = { rejectWithdrawalTarget = null },
            title = { Text("Reject & Refund ₦${target.amount}", color = DangerNeon) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Funds will be instantly returned to the player's eligible balance in Realtime Database.", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.rejectWithdrawal(target.id, rejectReason)
                        rejectWithdrawalTarget = null
                    }
                }) {
                    Text("Reject & Refund", color = DangerNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectWithdrawalTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }

    // Modal: Adjust User Balance
    adjustUserTarget?.let { target ->
        var coinAdjustment by remember { mutableStateOf("0") }
        var nairaAdjustment by remember { mutableStateOf("0.0") }
        var adjustmentReason by remember { mutableStateOf("Admin Bonus") }

        AlertDialog(
            onDismissRequest = { adjustUserTarget = null },
            title = { Text("Adjust Balance: ${target.username}", color = NeonCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = coinAdjustment,
                        onValueChange = { coinAdjustment = it },
                        label = { Text("Coins Delta (+/-)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nairaAdjustment,
                        onValueChange = { nairaAdjustment = it },
                        label = { Text("Naira Delta (+/- ₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adjustmentReason,
                        onValueChange = { adjustmentReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val c = coinAdjustment.toLongOrNull() ?: 0L
                    val n = nairaAdjustment.toDoubleOrNull() ?: 0.0
                    scope.launch {
                        repository.adminAdjustUserBalance(target.userId, c, n, adjustmentReason)
                        adjustUserTarget = null
                    }
                }) {
                    Text("Apply Adjustment", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustUserTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }

    // Modal: Add New Task
    if (showNewTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var taskTarget by remember { mutableStateOf("1") }
        var taskCoinReward by remember { mutableStateOf("50") }
        var taskNairaReward by remember { mutableStateOf("5.0") }

        AlertDialog(
            onDismissRequest = { showNewTaskDialog = false },
            title = { Text("Create New Task", color = NeonPurple) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskTarget,
                        onValueChange = { taskTarget = it },
                        label = { Text("Target Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskCoinReward,
                        onValueChange = { taskCoinReward = it },
                        label = { Text("Reward Coins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskNairaReward,
                        onValueChange = { taskNairaReward = it },
                        label = { Text("Reward Naira (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (taskTitle.isNotBlank()) {
                        val task = TaskItem(
                            title = taskTitle,
                            description = taskDesc,
                            target = taskTarget.toLongOrNull() ?: 1L,
                            rewardCoins = taskCoinReward.toLongOrNull() ?: 50L,
                            rewardEligible = taskNairaReward.toDoubleOrNull() ?: 5.0
                        )
                        scope.launch {
                            repository.addOrUpdateTask(task)
                            showNewTaskDialog = false
                        }
                    }
                }) {
                    Text("Add Task", color = NeonPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTaskDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NeonSurface
        )
    }

    // Modal: Add Announcement
    if (showNewAnnouncementDialog) {
        var annTitle by remember { mutableStateOf("") }
        var annContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewAnnouncementDialog = false },
            title = { Text("Post Global Announcement", color = NeonCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = annTitle,
                        onValueChange = { annTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = annContent,
                        onValueChange = { annContent = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (annTitle.isNotBlank() && annContent.isNotBlank()) {
                        val ann = Announcement(title = annTitle, content = annContent)
                        scope.launch {
                            repository.addAnnouncement(ann)
                            showNewAnnouncementDialog = false
                        }
                    }
                }) {
                    Text("Post", color = NeonCyan)
                }
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
