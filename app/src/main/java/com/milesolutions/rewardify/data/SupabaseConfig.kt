package com.milesolutions.rewardify.data

/**
 * Supabase project credentials.
 *
 * 1. Create a free project at https://supabase.com
 * 2. Open Project Settings -> API and copy the "Project URL" and "anon public" key
 * 3. Paste them below (the anon key is public by design — it is safe to ship in the app)
 */
object SupabaseConfig {
    const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"

    val isConfigured: Boolean
        get() = !SUPABASE_URL.startsWith("YOUR_") &&
            !SUPABASE_ANON_KEY.startsWith("YOUR_")
}
