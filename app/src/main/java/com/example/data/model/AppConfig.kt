package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppConfig(
    val minWithdrawalNgn: Double = 100.0,
    val currency: String = "NGN",
    val currencySymbol: String = "₦",
    val pointsToNgnRatio: Double = 0.01,
    val dailyRewardCoins: Long = 100L,
    val adMobEnabled: Boolean = true,
    val adMobBannerId: String = "ca-app-pub-3940256099942544/6300978111",
    val adMobInterstitialId: String = "ca-app-pub-3940256099942544/1033173712",
    val adMobRewardedId: String = "ca-app-pub-3940256099942544/5224354917",
    val rewardedAdCoins: Long = 50L,
    val rewardedAdNgn: Double = 5.0,
    val nigeriaOnlyEnabled: Boolean = true,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Neon Blast is currently undergoing scheduled maintenance. Please check back soon!",
    val announcementBanner: String = "🇳🇬 Welcome to Neon Blast Nigeria! Match blocks, clear lines, and withdraw cash rewards to your Nigerian bank account!",
    val secondChanceCoinCost: Long = 30L,
    val secondChanceAdReviveEnabled: Boolean = true,
    val referralRewardCoins: Long = 100L,
    val referralRewardNgn: Double = 20.0,
    val updatedAt: Long = System.currentTimeMillis()
)
