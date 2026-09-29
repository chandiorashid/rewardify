package com.milesolutions.rewardify.data

import android.content.Context
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Result of making sure the signed-in user has a referral profile. */
sealed interface EnsureProfileResult {
    /** No signed-in user — nothing to do. */
    data object NotLoggedIn : EnsureProfileResult

    /** Profile already existed (e.g. returning user, or pre-feature account). */
    data class Existing(val profile: ProfileRow) : EnsureProfileResult

    /**
     * Brand-new profile was registered.
     * @param bonusCredited true when the $0.20 referral welcome bonus was paid.
     * @param invalidCode true when a code was supplied but didn't match anyone.
     */
    data class Registered(
        val referralCode: String,
        val bonusCredited: Boolean,
        val invalidCode: Boolean
    ) : EnsureProfileResult
}

/**
 * Referral system backend, powered by the tables/functions in
 * supabase/referral_schema.sql:
 *
 * - New users get a unique referral code (generated server-side).
 * - Joining with a code pays the newcomer a $0.20 welcome bonus.
 * - Every task earning is tracked in the earnings ledger, and the referrer
 *   automatically receives 5% of it — for the lifetime of the referral.
 */
object ReferralRepository {

    const val SIGNUP_BONUS = 0.20
    const val COMMISSION_RATE = 0.05

    private val postgrest get() = Supabase.client.postgrest

    /**
     * Ensures the signed-in user has a profile row (and therefore a referral
     * code). Called once per sign-in/session — safe to call repeatedly, the
     * server side is idempotent.
     */
    suspend fun ensureProfile(context: Context): EnsureProfileResult {
        val userId = AuthRepository.currentUserId() ?: return EnsureProfileResult.NotLoggedIn

        getMyProfile()?.let { return EnsureProfileResult.Existing(it) }

        val pendingCode = ReferralPrefs.getPendingCode(context)
        val referrerId = pendingCode?.let { resolveReferrer(it) }

        val code = postgrest.rpc(
            function = "register_profile",
            parameters = buildJsonObject {
                put("p_display_name", AuthRepository.currentDisplayName() ?: "")
                // Nullable overload: null referrer is encoded as JSON null.
                put("p_referred_by", referrerId)
            }
        ).decodeSingleOrNull<CodeRow>()?.referral_code
            ?: throw IllegalStateException("Could not create your referral profile")

        ReferralPrefs.clearPendingCode(context)

        return EnsureProfileResult.Registered(
            referralCode = code,
            bonusCredited = referrerId != null,
            invalidCode = pendingCode != null && referrerId == null
        )
    }

    /** Resolves a referral code to the referrer's user id (null = unknown). */
    suspend fun resolveReferrer(code: String): String? {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return null
        return postgrest.rpc(
            function = "resolve_referrer",
            parameters = buildJsonObject { put("p_code", trimmed) }
        ).decodeSingleOrNull<ReferrerRow>()?.id
    }

    /** The signed-in user's profile, or null if none exists yet. */
    suspend fun getMyProfile(): ProfileRow? {
        val userId = AuthRepository.currentUserId() ?: return null
        return postgrest.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<ProfileRow>()
    }

    /** Users who signed up with my code, newest first. */
    suspend fun getMyReferrals(): List<ProfileRow> {
        val userId = AuthRepository.currentUserId() ?: return emptyList()
        return postgrest.from("profiles").select {
            filter { eq("referred_by", userId) }
        }.decodeList<ProfileRow>()
            .sortedByDescending { it.created_at ?: "" }
    }

    /** My full earnings ledger, newest first. */
    suspend fun getMyEarnings(): List<EarningRow> {
        val userId = AuthRepository.currentUserId() ?: return emptyList()
        return postgrest.from("earnings").select {
            filter { eq("user_id", userId) }
        }.decodeList<EarningRow>()
            .sortedByDescending { it.created_at ?: "" }
    }

    /** Lifetime total earned from referral commissions (the 5% payouts). */
    suspend fun getReferralEarningsTotal(): Double =
        getMyEarnings()
            .filter { it.source == "referral_commission" }
            .sumOf { it.amount }

    /** Current wallet balance = sum of the whole earnings ledger. */
    suspend fun getBalance(): Double =
        getMyEarnings().sumOf { it.amount }

    /**
     * Records a task earning for the signed-in user. The server also pays
     * 5% of [amount] to the user's referrer (when one exists) — forever.
     */
    suspend fun recordTaskEarning(amount: Double, note: String) {
        postgrest.rpc(
            function = "record_task_earning",
            parameters = buildJsonObject {
                put("p_amount", amount)
                put("p_note", note)
            }
        )
    }

    /**
     * Records a withdrawal for the signed-in user. The server writes a
     * negative 'withdrawal' row into the earnings ledger, so the balance
     * drops and the withdrawal is tracked in Supabase like every other
     * earning event. Throws when the balance doesn't cover [amount].
     *
     * Prefer [requestWithdrawal], which additionally creates a tracked
     * payout request (pending -> paid) against a saved payout method.
     */
    suspend fun recordWithdrawal(amount: Double, note: String) {
        postgrest.rpc(
            function = "record_withdrawal",
            parameters = buildJsonObject {
                put("p_amount", amount)
                put("p_note", note)
            }
        )
    }

    // ------------------------------------------------------------------
    // Payout methods & withdrawal requests
    // ------------------------------------------------------------------

    /** The signed-in user's payout methods, default first, newest next. */
    suspend fun getMyPayoutMethods(): List<PayoutMethodRow> {
        val userId = AuthRepository.currentUserId() ?: return emptyList()
        return postgrest.from("payout_methods").select {
            filter { eq("user_id", userId) }
        }.decodeList<PayoutMethodRow>()
            .sortedWith(
                compareByDescending<PayoutMethodRow> { it.is_default }
                    .thenByDescending { it.created_at ?: "" }
            )
    }

    /**
     * Saves a payout method. The first method a user adds automatically
     * becomes the default.
     */
    suspend fun addPayoutMethod(
        methodType: String,
        label: String,
        exchange: String?,
        accountRef: String,
        network: String?
    ): PayoutMethodRow {
        val userId = AuthRepository.currentUserId()
            ?: throw IllegalStateException("Not signed in")
        val makeDefault = getMyPayoutMethods().isEmpty()
        return postgrest.from("payout_methods")
            .insert(
                buildJsonObject {
                    put("user_id", userId)
                    put("method_type", methodType)
                    put("label", label)
                    put("exchange", exchange)
                    put("account_ref", accountRef)
                    put("network", network)
                    put("is_default", makeDefault)
                }
            ) {
                select()
            }.decodeSingle<PayoutMethodRow>()
    }

    /** Deletes one of the signed-in user's payout methods. */
    suspend fun deletePayoutMethod(id: String) {
        val userId = AuthRepository.currentUserId() ?: return
        postgrest.from("payout_methods").delete {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    /** Marks one of the user's payout methods as the default. */
    suspend fun setDefaultPayoutMethod(id: String) {
        postgrest.rpc(
            function = "set_default_payout_method",
            parameters = buildJsonObject { put("p_id", id) }
        )
    }

    /** The signed-in user's withdrawal requests, newest first. */
    suspend fun getMyWithdrawals(): List<WithdrawalRow> {
        val userId = AuthRepository.currentUserId() ?: return emptyList()
        return postgrest.from("withdrawals").select {
            filter { eq("user_id", userId) }
        }.decodeList<WithdrawalRow>()
            .sortedByDescending { it.requested_at ?: "" }
    }

    /**
     * Requests a withdrawal of [amount] to the given payout method.
     * Returns the withdrawal id. The server locks the funds immediately and
     * the request shows as "Pending" until the admin marks it paid in
     * Supabase (then the app shows "Received").
     */
    suspend fun requestWithdrawal(payoutMethodId: String, amount: Double): String {
        return postgrest.rpc(
            function = "request_withdrawal",
            parameters = buildJsonObject {
                put("p_payout_method_id", payoutMethodId)
                put("p_amount", amount)
            }
        ).decodeSingle<String>()
    }
}
