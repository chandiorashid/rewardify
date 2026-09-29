package com.milesolutions.rewardify.data

import android.content.Context

/**
 * Holds the referral code captured from a referral deep link until the new
 * user finishes signing up. The code is consumed (cleared) once the profile
 * is registered, so it never leaks into another account on the device.
 */
object ReferralPrefs {

    private const val PREFS = "rewardify_prefs"
    private const val KEY_PENDING_CODE = "pending_referral_code"

    fun savePendingCode(context: Context, code: String) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_CODE, code.trim().uppercase())
            .apply()
    }

    fun getPendingCode(context: Context): String? =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_CODE, null)
            ?.takeIf { it.isNotBlank() }

    fun clearPendingCode(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PENDING_CODE)
            .apply()
    }
}
