package com.milesolutions.rewardify.data

/**
 * AdMob configuration.
 *
 * Ships with Google's official SAMPLE ids, so rewarded ads work out of the
 * box in test mode. Before release:
 *  1. Create an AdMob account, register the Android app, and create a
 *     Rewarded ad unit.
 *  2. Replace [APP_ID] below AND the matching manifest meta-data
 *     (com.google.android.gms.ads.APPLICATION_ID in AndroidManifest.xml).
 *  3. Replace [REWARDED_AD_UNIT_ID] below with the real rewarded ad unit id.
 *
 * Never tap your own real ads during development — keep the sample ids (or
 * use test devices) until release, or AdMob may flag the account for
 * invalid activity.
 */
object AdsConfig {
    /** Sample id — replace with the real AdMob app id before release. */
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Sample id — replace with the real rewarded ad unit id before release. */
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /** One of these is granted at random for each fully-watched rewarded ad. */
    val AD_REWARDS = listOf(0.02, 0.018, 0.021)
}
