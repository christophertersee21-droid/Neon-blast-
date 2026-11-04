package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val referralCode: String = "",
    val referredBy: String = "",
    val bestScore: Long = 0L,
    val gamesPlayed: Long = 0L,
    val linesCleared: Long = 0L,
    val gameCoins: Long = 100L, // starter bonus coins
    val eligibleBalance: Double = 0.0,
    val pendingWithdrawal: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val dailyStreak: Long = 1L,
    val lastLoginDate: String = "",
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
