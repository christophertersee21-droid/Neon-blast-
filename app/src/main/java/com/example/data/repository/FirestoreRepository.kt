package com.example.data.repository

import android.content.Context
import com.example.data.model.Announcement
import com.example.data.model.AppConfig
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.GameSession
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.example.data.model.UserProfile
import com.example.data.model.UserTaskProgress
import com.example.data.model.WithdrawalRequest
import com.google.firebase.database.FirebaseDatabase

/**
 * Backwards-compatible alias & wrapper around RealtimeDatabaseRepository.
 * Redirects all data operations to Firebase Realtime Database:
 * https://com-aistudio-neonblast-r-d5875-default-rtdb.firebaseio.com/
 */
class FirestoreRepository(
    context: Context
) : RealtimeDatabaseRepository(context) {

    // Extra compatibility overload
    suspend fun getAppSettings(): AppSettings {
        val config = getAppConfig()
        return AppSettings(
            minWithdrawal = config.minWithdrawalNgn,
            dailyBonusCoins = config.dailyRewardCoins,
            secondChanceCoinCost = config.secondChanceCoinCost,
            maintenanceMode = config.maintenanceMode
        )
    }
}
