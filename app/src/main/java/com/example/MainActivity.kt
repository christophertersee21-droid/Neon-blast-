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
import com.example.ui.navigation.AppNavigation
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonBackground
import com.example.util.NetworkObserver
import com.example.util.OfflineRequiredScreen
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

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
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
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
