package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class GameSession(
    @DocumentId val id: String = "",
    val sessionId: String = "",
    val userId: String = "",
    val score: Long = 0L,
    val linesCleared: Long = 0L,
    val combos: Long = 0L,
    val durationSeconds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val rewardCoins: Long,
    val isUnlocked: Boolean,
    val currentProgress: Long,
    val target: Long
)
