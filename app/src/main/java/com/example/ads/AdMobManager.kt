package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"
    private var isInitialized = false

    // Standard AdMob Test Ad Unit IDs (Google Sample IDs)
    const val DEFAULT_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val DEFAULT_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val DEFAULT_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "AdMob MobileAds initialized: $status")
                    isInitialized = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize AdMob: ${e.message}")
            }
        }
    }

    fun showInterstitial(
        activity: Activity,
        adUnitId: String = DEFAULT_INTERSTITIAL_ID,
        onDismiss: () -> Unit
    ) {
        val targetId = if (adUnitId.isNotBlank()) adUnitId else DEFAULT_INTERSTITIAL_ID
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            targetId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            onDismiss()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                            onDismiss()
                        }
                    }
                    interstitialAd.show(activity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial failed to load: ${loadAdError.message}")
                    onDismiss()
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        adUnitId: String = DEFAULT_REWARDED_ID,
        onUserEarnedReward: (amount: Int) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val targetId = if (adUnitId.isNotBlank()) adUnitId else DEFAULT_REWARDED_ID
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            targetId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Rewarded failed to show: ${adError.message}")
                            onFailed(adError.message)
                        }
                    }
                    rewardedAd.show(activity) { rewardItem ->
                        onUserEarnedReward(rewardItem.amount)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    onFailed(loadAdError.message)
                }
            }
        )
    }
}
