package com.milesolutions.rewardify.data

import android.content.Context
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.buildJsonObject
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
}
