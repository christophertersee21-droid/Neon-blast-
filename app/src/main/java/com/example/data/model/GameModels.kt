package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GameSession(
    val id: String = "",
    val sessionId: String = "",
    val userId: String = "",
    val score: Long = 0L,
    val linesCleared: Long = 0L,
    val combos: Long = 0L,
    val durationSeconds: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

data class Achievement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconName: String = "",
    val rewardCoins: Long = 0L,
    val isUnlocked: Boolean = false,
    val currentProgress: Long = 0L,
    val target: Long = 0L
)
