package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

val ADMIN_EMAILS: Set<String> = setOf(
    "chrstphrnch@gmail.com",
    "christopherenoch767@gmail.com",
    "christophertersee21@gmail.com"
)

fun isUserAdminEmail(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    return ADMIN_EMAILS.contains(email.trim().lowercase())
}

@IgnoreExtraProperties
data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val referralCode: String = "",
    val referredBy: String = "",
    val bestScore: Long = 0L,
    val gamesPlayed: Long = 0L,
    val linesCleared: Long = 0L,
    val gameCoins: Long = 500L, // Starter bonus coins
    val eligibleBalance: Double = 150.0, // Starter Naira balance (₦150) so minimum ₦100 withdrawal can be tested immediately!
    val pendingWithdrawal: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val dailyStreak: Long = 1L,
    val lastLoginDate: String = "",
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val country: String = "NG",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun hasAdminPrivileges(): Boolean {
        return isAdmin || isUserAdminEmail(email)
    }
}
