package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TaskItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "daily", // daily, weekly, special, referral
    val target: Long = 1L,
    val rewardCoins: Long = 50L,
    val rewardEligible: Double = 0.0,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserTaskProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val taskId: String = "",
    val currentProgress: Long = 0L,
    val target: Long = 1L,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
