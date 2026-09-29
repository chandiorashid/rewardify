package com.milesolutions.rewardify.data

/** Earning task categories. */
enum class TaskCategory(val label: String) {
    ALL("All"),
    SURVEY("Surveys"),
    OFFER("Offers"),
    REFERRAL("Referrals"),
    CHECKIN("Check-in")
}

/** A single earning opportunity. */
data class TaskItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val reward: Double,
    val category: TaskCategory,
    val progress: Float? = null // null = not started, 0..1 = in progress
)

/** A wallet ledger entry. Positive amount = credit, negative = debit. */
data class Transaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: Double
)

/** Earning category shown on the Home grid. */
data class EarningCategory(
    val label: String,
    val iconName: String,
    val category: TaskCategory
)

/** Profile menu row. */
data class ProfileMenuItem(
    val label: String,
    val iconName: String,
    val isDestructive: Boolean = false
)
