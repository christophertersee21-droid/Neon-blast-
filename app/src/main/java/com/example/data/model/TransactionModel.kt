package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TransactionItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "", // ELIGIBLE_REWARD, GAME_COIN_REWARD, WITHDRAWAL_REQUEST, WITHDRAWAL_APPROVED, WITHDRAWAL_REJECTED, WITHDRAWAL_PAID, ADMIN_ADJUSTMENT
    val amount: Double = 0.0,
    val currency: String = "USD", // USD or COINS
    val balanceAfter: Double = 0.0,
    val status: String = "COMPLETED", // COMPLETED, PENDING, APPROVED, REJECTED, PAID
    val description: String = "",
    val reference: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
