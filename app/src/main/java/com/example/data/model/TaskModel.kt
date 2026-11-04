package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TaskItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "daily", // daily, weekly, special, referral
    val target: Long = 1L,
    val rewardCoins: Long = 50L,
    val rewardEligible: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class UserTaskProgress(
    val id: String = "",
    val userId: String = "",
    val taskId: String = "",
    val currentProgress: Long = 0L,
    val target: Long = 1L,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
