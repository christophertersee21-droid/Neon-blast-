package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Announcement(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = "",
    val actionUrl: String = "",
    val badge: String = "UPDATE",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "SYSTEM", // WITHDRAWAL, TASK, CHALLENGE, SYSTEM
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppSettings(
    @DocumentId val id: String = "global",
    val minWithdrawal: Double = 10.0,
    val maxWithdrawal: Double = 500.0,
    val maintenanceMode: Boolean = false,
    val dailyBonusCoins: Long = 100L,
    val comboMultiplierBonus: Long = 10L,
    val secondChanceCoinCost: Long = 50L
)
