package com.milesolutions.rewardify.data

import kotlinx.serialization.Serializable

/** Row from public.profiles. */
@Serializable
data class ProfileRow(
    val id: String,
    val display_name: String? = null,
    val referral_code: String,
    val referred_by: String? = null,
    val created_at: String? = null
)

/** Single-column result of register_profile(). */
@Serializable
data class CodeRow(val referral_code: String)

/** Single-column result of resolve_referrer(). */
@Serializable
data class ReferrerRow(val id: String)

/**
 * Row from public.earnings — the ledger every earning event is tracked in.
 * source: task | signup_bonus | referral_commission | withdrawal | adjustment
 */
@Serializable
data class EarningRow(
    val id: String,
    val user_id: String,
    val amount: Double,
    val source: String,
    val related_user_id: String? = null,
    val note: String? = null,
    val created_at: String? = null
)
