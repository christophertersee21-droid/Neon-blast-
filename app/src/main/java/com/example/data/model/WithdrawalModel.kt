package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class WithdrawalRequest(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val amount: Double = 100.0,
    val currency: String = "NGN",
    val payoutMethod: String = "Nigerian Bank Transfer", // Nigerian Bank Transfer, OPay, PalmPay, Kuda
    val bankName: String = "Access Bank",
    val accountNumber: String = "",
    val accountName: String = "",
    val payoutDetails: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, PAID
    val rejectionReason: String = "",
    val paymentReference: String = "",
    val reference: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
