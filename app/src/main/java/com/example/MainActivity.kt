package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ads.AdMobManager
import com.example.data.model.AppConfig
import com.example.data.repository.FirestoreRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.region.NigeriaOnlyRestrictedScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonBackground
import com.example.util.NetworkObserver
import com.example.util.NigeriaRegionValidator
import com.example.util.OfflineRequiredScreen
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AdMob Mobile Ads SDK
        AdMobManager.initialize(this)

        // Configure App Check before UI rendering
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NeonBackground
                ) {
                    val context = LocalContext.current
                    val networkObserver = remember { NetworkObserver(context) }
                    val isOnline by networkObserver.observe().collectAsState(initial = networkObserver.isCurrentlyConnected())

                    if (!isOnline) {
                        OfflineRequiredScreen(
                            onRetry = {
                                // Re-evaluates on network change
                            }
                        )
                    } else {
                        MainAppGate()
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppGate() {
    val context = LocalContext.current
    val repository = remember { FirestoreRepository(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var locationCheckTrigger by remember { mutableStateOf(0) }
    var adminBypassPortal by remember { mutableStateOf(false) }

    val config by repository.observeAppConfig().collectAsState(initial = AppConfig())

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null || adminBypassPortal) {
        AuthScreen(
            onAuthSuccess = {
                adminBypassPortal = false
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        // Enforce Nigeria geography requirement (with admin bypass)
        val isAllowedInNigeria = remember(user.email, config.nigeriaOnlyEnabled, locationCheckTrigger) {
            NigeriaRegionValidator.isAllowedInNigeria(
                context = context,
                userEmail = user.email,
                nigeriaRestrictionActive = config.nigeriaOnlyEnabled
            )
        }

        if (config.nigeriaOnlyEnabled && !isAllowedInNigeria) {
            NigeriaOnlyRestrictedScreen(
                onRetry = {
                    locationCheckTrigger++
                },
                onAdminLoginBypass = {
                    adminBypassPortal = true
                }
            )
        } else {
            AppNavigation(
                currentUserId = user.uid,
                onSignOut = {
                    currentUser = null
                }
            )
        }
    }
}
