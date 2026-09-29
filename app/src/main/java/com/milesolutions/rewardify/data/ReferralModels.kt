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

/**
 * Row from public.payout_methods — a saved payout destination.
 * method_type: "usdc" (account_ref = wallet address, network set) |
 *              "exchange" (exchange = binance/okx/..., account_ref = UID)
 */
@Serializable
data class PayoutMethodRow(
    val id: String,
    val user_id: String,
    val method_type: String,
    val label: String,
    val exchange: String? = null,
    val account_ref: String,
    val network: String? = null,
    val is_default: Boolean = false,
    val created_at: String? = null
)

/**
 * Row from public.install_offers — an admin-managed "install & try" task.
 */
@Serializable
data class InstallOfferRow(
    val id: String,
    val app_name: String,
    val package_name: String,
    val store_url: String,
    val reward: Double = 0.08,
    val instructions: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
)

/**
 * Row from public.task_submissions — screenshot proof filed by a user.
 * status: "pending" | "approved" (reward auto-credited) | "rejected"
 */
@Serializable
data class TaskSubmissionRow(
    val id: String,
    val user_id: String,
    val offer_id: String,
    val screenshot_urls: List<String> = emptyList(),
    val status: String,
    val admin_note: String? = null,
    val submitted_at: String? = null,
    val decided_at: String? = null
)

/**
 * Row from public.withdrawals — a withdrawal request and its payout status.
 * status: "pending" (user sees Pending) | "paid" (user sees Received) |
 *         "rejected"
 */
@Serializable
data class WithdrawalRow(
    val id: String,
    val user_id: String,
    val payout_method_id: String? = null,
    val amount: Double,
    val method_type: String,
    val method_label: String,
    val account_ref: String,
    val exchange: String? = null,
    val network: String? = null,
    val status: String,
    val admin_note: String? = null,
    val requested_at: String? = null,
    val decided_at: String? = null
)
