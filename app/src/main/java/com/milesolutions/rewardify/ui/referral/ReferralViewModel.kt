package com.milesolutions.rewardify.ui.referral

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milesolutions.rewardify.data.ReferralRepository
import com.milesolutions.rewardify.data.Supabase
import kotlinx.coroutines.launch

data class ReferralListItem(
    val name: String,
    val joinedLabel: String,
    val earnedFromThem: Double
)

data class ReferralUiState(
    val isLoading: Boolean = true,
    /** Non-null when the referral backend isn't reachable (e.g. SQL not run yet). */
    val error: String? = null,
    val code: String = "",
    val referralLink: String = "",
    val totalReferrals: Int = 0,
    val referralEarnings: Double = 0.0,
    val referrals: List<ReferralListItem> = emptyList()
)

class ReferralViewModel : ViewModel() {

    var uiState by mutableStateOf(ReferralUiState())
        private set

    fun refresh(context: Context) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            uiState = try {
                // New accounts get their profile the moment they sign in
                // (MainActivity), so by now a code should exist. If not,
                // create one on the fly so the screen is never empty.
                var profile = ReferralRepository.getMyProfile()
                if (profile == null) {
                    ReferralRepository.ensureProfile(context.applicationContext)
                    profile = ReferralRepository.getMyProfile()
                }
                val code = profile?.referral_code.orEmpty()

                val referrals = ReferralRepository.getMyReferrals()
                val commissions = ReferralRepository.getMyEarnings()
                    .filter { it.source == "referral_commission" }
                val earnedByReferrer = commissions
                    .groupBy { it.related_user_id }
                    .mapValues { (_, rows) -> rows.sumOf { it.amount } }

                uiState.copy(
                    isLoading = false,
                    code = code,
                    referralLink = Supabase.referralLink(code),
                    totalReferrals = referrals.size,
                    referralEarnings = commissions.sumOf { it.amount },
                    referrals = referrals.map { row ->
                        ReferralListItem(
                            name = row.display_name?.takeIf { it.isNotBlank() }
                                ?: "Member ${row.referral_code.take(4)}",
                            joinedLabel = row.created_at?.take(10) ?: "",
                            earnedFromThem = earnedByReferrer[row.id] ?: 0.0
                        )
                    }
                )
            } catch (e: Exception) {
                uiState.copy(
                    isLoading = false,
                    error = "Couldn't load your referral data. Check your connection and try again."
                )
            }
        }
    }
}
