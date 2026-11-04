package com.example.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.NeonBackground
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.DEFAULT_BANNER_ID,
    isEnabled: Boolean = true
) {
    if (!isEnabled) return

    val targetId = if (adUnitId.isNotBlank()) adUnitId else AdMobManager.DEFAULT_BANNER_ID

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(NeonBackground)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    setAdUnitId(targetId)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
