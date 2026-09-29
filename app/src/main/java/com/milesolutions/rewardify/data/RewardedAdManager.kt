package com.milesolutions.rewardify.data

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Loads and shows AdMob rewarded ads. Process-wide singleton: the Tasks
 * screen drives it and a fresh ad is pre-loaded after every show/dismiss.
 */
object RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private var loading = false

    val isReady: Boolean get() = rewardedAd != null

    /**
     * Starts loading a rewarded ad unless one is already loaded/loading.
     * [onResult] reports the outcome (true = ready to show).
     */
    fun load(context: Context, onResult: ((Boolean) -> Unit)? = null) {
        if (loading || rewardedAd != null) {
            onResult?.invoke(rewardedAd != null)
            return
        }
        loading = true
        RewardedAd.load(
            context,
            AdsConfig.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loading = false
                    rewardedAd = ad
                    onResult?.invoke(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    rewardedAd = null
                    onResult?.invoke(false)
                }
            }
        )
    }

    /**
     * Shows the loaded ad. [onReward] fires only when the user watches long
     * enough to earn the reward; [onFinished] fires when the ad closes either
     * way (used to reset UI state and pre-load the next ad).
     */
    fun show(
        activity: Activity,
        onReward: () -> Unit,
        onFinished: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onFinished()
            return
        }
        rewardedAd = null // consume: one show per load
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            // Note: no auto-reload here — the caller reloads in onFinished
            // so its readiness callback fires correctly.
            override fun onAdDismissedFullScreenContent() = onFinished()
            override fun onAdFailedToShowFullScreenContent(
                error: com.google.android.gms.ads.AdError
            ) = onFinished()
        }
        ad.show(activity) { onReward() }
    }
}
