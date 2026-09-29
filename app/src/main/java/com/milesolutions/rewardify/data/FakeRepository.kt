package com.milesolutions.rewardify.data

/**
 * Demo data source. Replace with a real repository (Retrofit / Room)
 * when wiring Rewardify up to a backend.
 */
object FakeRepository {

    val userName = "Alex Johnson"
    val userFirstName = "Alex"
    val userEmail = "alex@example.com"
    val membershipTier = "Gold member"

    const val availableBalance = 24.50
    const val pendingBalance = 3.75
    const val lifetimeEarned = 86.20
    const val earnedThisWeek = 3.20

    const val tasksCompleted = 37
    const val totalReferrals = 12

    val categories = listOf(
        EarningCategory("Surveys", "assignment", TaskCategory.SURVEY),
        EarningCategory("Offers", "local_offer", TaskCategory.OFFER),
        EarningCategory("Referrals", "group", TaskCategory.REFERRAL),
        EarningCategory("Daily check-in", "event_available", TaskCategory.CHECKIN)
    )

    val featuredTasks = listOf(
        TaskItem(
            id = "t1",
            title = "Complete a 5-min survey",
            subtitle = "~5 min · 2,300 spots left",
            reward = 1.50,
            category = TaskCategory.SURVEY
        ),
        TaskItem(
            id = "t2",
            title = "Try a new shopping app",
            subtitle = "~10 min · Install & open",
            reward = 2.00,
            category = TaskCategory.OFFER
        )
    )

    val tasks = listOf(
        TaskItem(
            id = "t1",
            title = "Watch & review a product video",
            subtitle = "~5 min · 1,120 spots left",
            reward = 2.00,
            category = TaskCategory.SURVEY
        ),
        TaskItem(
            id = "t2",
            title = "Install and open FitTrack app",
            subtitle = "~10 min · 2,300 spots left",
            reward = 1.50,
            category = TaskCategory.OFFER,
            progress = 0.6f
        ),
        TaskItem(
            id = "t3",
            title = "Share your referral link",
            subtitle = "Earn for every friend who joins",
            reward = 5.00,
            category = TaskCategory.REFERRAL
        ),
        TaskItem(
            id = "t4",
            title = "Daily check-in · Day 5",
            subtitle = "Keep your streak going",
            reward = 0.25,
            category = TaskCategory.CHECKIN
        ),
        TaskItem(
            id = "t5",
            title = "Answer a lifestyle questionnaire",
            subtitle = "~8 min · 640 spots left",
            reward = 1.75,
            category = TaskCategory.SURVEY
        ),
        TaskItem(
            id = "t6",
            title = "Reach level 10 in Puzzle Quest",
            subtitle = "~30 min · Gaming offer",
            reward = 4.50,
            category = TaskCategory.OFFER
        )
    )

    val transactions = listOf(
        Transaction("x1", "Survey reward", "Today, 2:14 PM", 2.00),
        Transaction("x2", "Referral bonus", "Yesterday", 5.00),
        Transaction("x3", "Withdrawal to bank", "Sep 24", -10.00),
        Transaction("x4", "Daily check-in", "Sep 23", 0.25),
        Transaction("x5", "Offer completion", "Sep 21", 1.50)
    )

    val profileMenu = listOf(
        ProfileMenuItem("Edit profile", "person"),
        ProfileMenuItem("Payout methods", "account_balance"),
        ProfileMenuItem("Notifications", "notifications"),
        ProfileMenuItem("Refer & earn", "card_giftcard"),
        ProfileMenuItem("Help & support", "help"),
        ProfileMenuItem("Privacy & terms", "shield"),
        ProfileMenuItem("Log out", "logout", isDestructive = true)
    )
}
