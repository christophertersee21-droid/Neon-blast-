package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.repository.AiStrategyRepository
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.NeonGalaxyBackground
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.game.GameScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.notifications.NotificationsDialog
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.NeonBackground
import com.example.ui.theme.NeonCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonSurface
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PLAY("Play", Icons.Default.PlayArrow),
    TASKS("Tasks", Icons.Default.Task),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet),
    PROFILE("Profile", Icons.Default.Person)
}

enum class SubScreen {
    NONE, ADMIN, TRANSACTIONS
}

@Composable
fun AppNavigation(
    currentUserId: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FirestoreRepository(context) }
    val aiRepository = remember { AiStrategyRepository() }

    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Realtime state observations
    val userProfile by repository.observeUserProfile(currentUserId).collectAsState(initial = null)
    val tasks by repository.observeTasks().collectAsState(initial = emptyList())
    val userTasks by repository.observeUserTasks(currentUserId).collectAsState(initial = emptyList())
    val transactions by repository.observeTransactions(currentUserId).collectAsState(initial = emptyList())
    val withdrawals by repository.observeWithdrawals(currentUserId).collectAsState(initial = emptyList())
    val announcements by repository.observeAnnouncements().collectAsState(initial = emptyList())
    val notifications by repository.observeNotifications(currentUserId).collectAsState(initial = emptyList())
    val allUsers by repository.observeAllUsers().collectAsState(initial = emptyList())
    val allWithdrawals by repository.observeAllWithdrawals().collectAsState(initial = emptyList())

    // Initial user sync and seeding
    LaunchedEffect(currentUserId) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            repository.getOrCreateUserProfile(
                email = user.email ?: "",
                displayName = user.displayName ?: "",
                photoUrl = user.photoUrl?.toString() ?: ""
            )
            repository.seedDefaultsIfNeeded()
        }
    }

    // Handle back button for sub-screens or sub-tabs
    BackHandler(enabled = currentSubScreen != SubScreen.NONE || currentTab != ScreenTab.HOME) {
        if (currentSubScreen != SubScreen.NONE) {
            currentSubScreen = SubScreen.NONE
        } else if (currentTab != ScreenTab.HOME) {
            currentTab = ScreenTab.HOME
        }
    }

    val profile = userProfile
    if (profile == null) {
        NeonGalaxyBackground {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        }
        return
    }

    // Full screen overlay for Game tab or Subscreens
    when {
        currentSubScreen == SubScreen.ADMIN -> {
            AdminDashboardScreen(
                users = allUsers,
                withdrawals = allWithdrawals,
                announcements = announcements,
                repository = repository,
                onBackClicked = { currentSubScreen = SubScreen.NONE }
            )
            return
        }

        currentSubScreen == SubScreen.TRANSACTIONS -> {
            TransactionsScreen(
                transactions = transactions,
                onBackClicked = { currentSubScreen = SubScreen.NONE }
            )
            return
        }

        currentTab == ScreenTab.PLAY -> {
            GameScreen(
                user = profile,
                repository = repository,
                aiRepository = aiRepository,
                onBackClicked = { currentTab = ScreenTab.HOME }
            )
            return
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = NeonSurface,
                modifier = Modifier.border(1.dp, NeonCardBorder)
            ) {
                ScreenTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(tab.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = NeonCyan.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = NeonBackground
    ) { paddingValues ->
        NeonGalaxyBackground(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        user = profile,
                        repository = repository,
                        announcements = announcements,
                        unreadNotificationCount = notifications.count { !it.isRead },
                        onPlayClicked = { currentTab = ScreenTab.PLAY },
                        onWalletClicked = { currentTab = ScreenTab.WALLET },
                        onTasksClicked = { currentTab = ScreenTab.TASKS },
                        onNotificationsClicked = { showNotificationsDialog = true },
                        onAdminClicked = { currentSubScreen = SubScreen.ADMIN }
                    )
                }

                ScreenTab.TASKS -> {
                    TasksScreen(
                        user = profile,
                        tasks = tasks,
                        userTasks = userTasks,
                        repository = repository
                    )
                }

                ScreenTab.WALLET -> {
                    WalletScreen(
                        user = profile,
                        withdrawals = withdrawals,
                        repository = repository,
                        onViewTransactionsClicked = { currentSubScreen = SubScreen.TRANSACTIONS }
                    )
                }

                ScreenTab.PROFILE -> {
                    ProfileScreen(
                        user = profile,
                        onSignOutClicked = {
                            Firebase.auth.signOut()
                            onSignOut()
                        },
                        onAdminClicked = { currentSubScreen = SubScreen.ADMIN }
                    )
                }

                ScreenTab.PLAY -> {} // handled above
            }
        }
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            repository = repository,
            onDismiss = { showNotificationsDialog = false }
        )
    }
}
