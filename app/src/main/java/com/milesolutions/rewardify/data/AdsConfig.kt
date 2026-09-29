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
 *
 * META AUDIENCE NETWORK MEDIATION (bidding):
 * The Meta mediation adapter is bundled via Gradle
 * (com.google.ads.mediation:facebook) — no code changes are needed for it.
 * Meta placement ids are NOT placed in code; they are entered server-side:
 *  1. Meta Monetization Manager (business.facebook.com): create the app and
 *     a Rewarded placement -> copy the Placement ID.
 *  2. AdMob dashboard -> Mediation -> create a mediation group for the
 *     rewarded ad unit above -> add "Meta Audience Network" as a BIDDING
 *     ad source -> paste the Placement ID.
 *  3. Testing Meta ads: Meta has no universal sample placement ids. Instead,
 *     register your device as a test device in Meta's Monetization Manager
 *     (or use AdMob test-device mode) — test devices receive Meta test ads
 *     on your real placement ids.
 */
object AdsConfig {
    /** Sample id — replace with the real AdMob app id before release. */
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Sample id — replace with the real rewarded ad unit id before release. */
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /** One of these is granted at random for each fully-watched rewarded ad. */
    val AD_REWARDS = listOf(0.02, 0.018, 0.021)
}
