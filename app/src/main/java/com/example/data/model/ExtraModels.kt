package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val actionUrl: String = "",
    val badge: String = "UPDATE",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "SYSTEM", // WITHDRAWAL, TASK, CHALLENGE, SYSTEM, AD_REWARD
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class AppSettings(
    val id: String = "global",
    val minWithdrawal: Double = 100.0,
    val maxWithdrawal: Double = 500000.0,
    val maintenanceMode: Boolean = false,
    val dailyBonusCoins: Long = 100L,
    val comboMultiplierBonus: Long = 10L,
    val secondChanceCoinCost: Long = 30L
)
