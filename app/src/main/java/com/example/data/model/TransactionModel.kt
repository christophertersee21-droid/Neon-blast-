package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TransactionItem(
    val id: String = "",
    val userId: String = "",
    val type: String = "", // ELIGIBLE_REWARD, GAME_COIN_REWARD, WITHDRAWAL_REQUEST, WITHDRAWAL_APPROVED, WITHDRAWAL_REJECTED, ADMIN_ADJUSTMENT
    val amount: Double = 0.0,
    val currency: String = "NGN", // NGN or COINS
    val balanceAfter: Double = 0.0,
    val status: String = "COMPLETED", // COMPLETED, PENDING, APPROVED, REJECTED
    val description: String = "",
    val reference: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
