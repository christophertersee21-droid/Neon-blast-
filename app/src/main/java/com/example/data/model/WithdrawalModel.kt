package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WithdrawalRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val payoutMethod: String = "PayPal", // PayPal, Bank Transfer, Crypto, Gift Card
    val payoutDetails: String = "",
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, APPROVED, PROCESSING, PAID, REJECTED, CANCELLED
    val rejectionReason: String = "",
    val paymentReference: String = "",
    val reference: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
