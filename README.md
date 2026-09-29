# Rewardify — Android App (Kotlin + Jetpack Compose)

A modern, clean, trustworthy rewards/earning application built with **Kotlin** and
**Jetpack Compose (Material 3)**. Package: `com.milesolutions.rewardify`.

## Screens

| Area | Screens |
|---|---|
| Auth | Login, Signup |
| Main (bottom navigation) | Home, Tasks, Wallet, Profile |

The UI follows the approved mockup design system:
- **Primary:** deep indigo (trust, navigation, CTAs)
- **Accent:** emerald green (earnings, success states)
- **Brand:** gold coin-gift logo mark
- Rounded cards (16–24dp), gradient balance cards, reward badges (`+$2.00`)

## How to open in Android Studio

1. Download and unzip `Rewardify.zip` (or clone/copy this folder).
2. In Android Studio: **File → Open…** and select the `rewardify_app` folder.
3. Let Gradle sync (it downloads Gradle 8.7 and dependencies on first run — needs internet).
4. Press **Run ▶** (or Shift+F10) with an emulator or a connected device.

Requirements: Android Studio Hedgehog (2023.1.1) or newer, JDK 17 (bundled with
Android Studio), minSdk 26 / targetSdk 34.

## Project structure

```
rewardify_app/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/values/themes.xml
        └── java/com/milesolutions/rewardify/
            ├── MainActivity.kt
            ├── data/
            │   ├── Models.kt            # TaskItem, Transaction, EarningCategory…
            │   └── FakeRepository.kt    # Demo data (swap for a real backend)
            ├── ui/theme/
            │   ├── Color.kt             # Indigo / emerald / gold palette
            │   ├── Type.kt
            │   └── Theme.kt             # RewardifyTheme (light + dark)
            ├── ui/components/
            │   └── Components.kt        # Logo, buttons, fields, cards, bottom bar…
            ├── ui/screens/
            │   ├── LoginScreen.kt
            │   ├── SignupScreen.kt
            │   ├── HomeScreen.kt
            │   ├── TasksScreen.kt
            │   ├── WalletScreen.kt
            │   └── ProfileScreen.kt
            └── ui/navigation/
                └── NavGraph.kt          # Auth flow + bottom-nav tab graph
```

## Supabase authentication setup (required)

Login and Signup now use **real Supabase Auth** (email/password). The app will not
be able to log anyone in until you complete these steps:

1. Create a free project at https://supabase.com (takes ~2 minutes).
2. In the Supabase dashboard, go to **Project Settings → API** and copy:
   - **Project URL** (looks like `https://xyzcompany.supabase.co`)
   - **anon public key** (the long `eyJ...` token — it is public by design, safe to ship in the app)
3. Open `app/src/main/java/com/milesolutions/rewardify/data/SupabaseConfig.kt`
   and paste both values into `SUPABASE_URL` and `SUPABASE_ANON_KEY`.
4. In the dashboard go to **Authentication → Providers → Email** and make sure
   Email is enabled (it is by default).
5. Decide on email confirmation (**Authentication → Settings**):
   - Keep **"Confirm email" ON** (default): after signup the user gets a
     confirmation email. Tapping the link opens the Rewardify app and signs
     them in automatically — no localhost error. ⚠️ One dashboard step is
     required for this: go to **Authentication → URL Configuration → Redirect
     URLs** and add exactly this URL, then Save:
     `com.milesolutions.rewardify://auth-callback`
     (Without it, Supabase falls back to the default Site URL
     `http://localhost:3000`, which cannot open on a phone.)
   - Turn it **OFF** for instant access: signup logs the user straight in.

That is everything needed from your side — no other config, no extra libraries.
Until the keys are pasted, the app shows a toast saying Supabase is not configured.

## Referral system setup (required for referrals & earnings)

The referral system (5% lifetime commission + $0.20 signup bonus) needs three
database tables/functions. This is a one-time, one-minute step:

1. In the Supabase dashboard open the **SQL Editor → New query**.
2. Open the file `supabase/referral_schema.sql` from this repo, copy its entire
   contents, paste into the query editor and press **Run**.
3. Done — no errors means the backend is live.

## Payouts & withdrawals setup

Withdrawals support two payout rails: **USDC** (wallet address + network) and
**exchange UIDs** (Binance, OKX, Bybit, …). The backend for this is
`supabase/payout_schema.sql` — already deployed; run it in the SQL Editor only
if you're setting up a fresh project.

How it works:

- **Payout methods screen** (Wallet → "Add payout method"): users save a USDC
  wallet (address + network: Solana, Ethereum, Polygon, …) and/or exchange
  accounts (exchange + UID). Tap a method to make it the default; the first one
  added becomes the default automatically.
- **Requesting a withdrawal:** Wallet → Withdraw (Home's Withdraw button jumps
  to Wallet) → pick a payout method → enter an amount (min $5.00). If the user
  has no payout method yet, the app sends them to add one first.
- The server locks the funds **immediately** (negative `withdrawal` ledger row,
  balance drops, $5 minimum and sufficient-balance enforced server-side) and
  creates a `withdrawals` row with status **pending**. The app history shows it
  with a gold **Pending** badge, and the Wallet "Pending" stat shows the real
  pending total.
- **Paying out (admin):** open the Supabase dashboard → Table Editor →
  `withdrawals`. Each pending row shows the amount and exactly where to send it
  (`method_label`, `account_ref`, `exchange`/`network`). Send the money manually
  (USDC transfer or exchange internal transfer to the UID), then edit the row:
  set `status` to `paid` (and optionally `decided_at` = now, `admin_note`).
- On next open, the app shows that withdrawal with a green **Received** badge.

## Rewarded ads setup (AdMob)

The Tasks screen has a **Watch Ad** card: each fully-watched rewarded ad grants
one of **$0.02 / $0.018 / $0.021 at random**, written straight into the Supabase
earnings ledger (with the usual 5% lifetime commission to the referrer).

The app ships with **Google's official sample ad ids**, so ads work in test mode
out of the box. Before release:

1. Create an [AdMob](https://admob.google.com) account, register the Android
   app, and create a **Rewarded** ad unit.
2. In `app/src/main/java/com/milesolutions/rewardify/data/AdsConfig.kt`:
   replace `APP_ID` and `REWARDED_AD_UNIT_ID` with your real ids.
3. In `app/src/main/AndroidManifest.xml`: replace the
   `com.google.android.gms.ads.APPLICATION_ID` meta-data value with the same
   real app id (keep it in sync with `AdsConfig.APP_ID`).

Never tap your own real ads during development — keep the sample ids (or use
AdMob test devices) until release, or AdMob may flag the account for invalid
activity.

## Install & earn offers (screenshot proof + manual approval)

The Tasks screen also lists **install offers** — admin-managed tasks where a
user installs an app from the Play Store, uses it ~2 minutes, and submits
screenshots as proof. You review and approve manually; on approval the **$0.08**
reward is credited automatically (plus 5% to the referrer). The backend is
`supabase/task_submissions_schema.sql` — already deployed.

How it works:

- **Adding an offer (admin):** Supabase dashboard → Table Editor →
  `install_offers` → Insert row: `app_name`, `package_name`, `store_url`
  (full Play Store link), `reward` (default 0.08), `instructions`, `is_active`
  = true. It appears in the app immediately — no app update needed.
- **User flow:** Tasks → "Install & earn" → tap an offer → "Open Play Store"
  → install & use the app → "Submit screenshots" → pick up to 5 screenshots
  (installed app, screens visited) → uploads to the private `task-proofs`
  bucket and files a **pending** submission.
- **Reviewing (admin):** Table Editor → `task_submissions` shows each pending
  row with its screenshot paths; open the images in Storage → `task-proofs` →
  `<user-id>/`. Set `status` to `approved` (or `rejected` with an `admin_note`).
- On **approved**, a trigger credits the offer's reward to the user instantly
  — the app shows a green **Approved** badge in "My submissions" and the $0.08
  lands in their wallet. The trigger only fires on the pending → approved
  transition, so re-editing can never double-pay.
- Users can only ever see/file their **own** submissions; screenshots live in
  a private bucket (one folder per user).
  Rejected payouts can be marked `rejected` (red badge).
- Everything is server-side (profiles, ledger, payout methods, withdrawals), so
  logout / reinstall / new phone + sign-in restores balance, methods and the
  full pending/paid history.

How it works:

- **Every user gets a unique referral code**, generated server-side on first
  sign-in (existing accounts get one automatically too).
- **Newcomer bonus:** signing up with a referral code (typed in, or via a
  referral link like `com.milesolutions.rewardify://referral?code=ABC123`)
  credits the new user **$0.20** immediately after their first sign-in.
- **Lifetime 5% commission:** every earning is tracked in the `earnings` ledger.
  When a referred user completes a task, the server automatically pays
  **5% of that earning to the referrer — forever**. Commissions land in the
  referrer's wallet automatically and appear on the Refer & Earn screen and in
  Wallet → Recent transactions.
- Commissions are calculated on **task earnings only** (not on bonuses).
- **Refer & Earn screen** (Profile → "Refer & earn", or the Home promo card):
  shows your code, copy/share buttons, total referrals, lifetime referral
  earnings, how-it-works steps, and a per-referral breakdown.
- **Completing a task** now records a real earning: tap a task → Start →
  confirm "Complete task?" → the amount is logged and the referrer's 5% is
  paid instantly.
- **Withdrawals are tracked too:** requesting a withdrawal locks the funds with
  a negative `withdrawal` ledger row (via `supabase/payout_schema.sql`) and
  creates a `withdrawals` row with status **pending**; marking it `paid` in the
  Supabase Table Editor flips the app history to **Received**. See "Payouts &
  withdrawals setup" above.
- The Wallet balance and Recent transactions read from the real ledger once
  earnings exist (demo values are shown until then).
- **Everything is server-side:** the ledger lives in Supabase keyed by account,
  so logging out, deleting the app, or moving to a new phone and signing back
  in restores the exact same balance, earnings and referral data.

## App logic included

- **Login/Signup validation** — empty-field, email-format, password-length and
  password-match checks with toast feedback; forgot-password and social buttons
  show contextual toasts.
- **Withdraw dialog** — payout-method picker (USDC / exchange UID) plus amount
  entry with validation (valid number, $5.00 minimum, sufficient balance);
  confirming calls `request_withdrawal`, which locks the funds server-side and
  creates a **Pending** withdrawal; history badges flip to **Received** once the
  admin marks it paid in Supabase. Lives in Wallet (Home's Withdraw button
  jumps to Wallet).
- **Gift-card redemption dialog** — code validation with a success toast.
- **Tasks** — category tabs filter the list; tapping a Home category tile opens
  Tasks pre-filtered to that category; confirming "Complete task?" records a
  real earning in the ledger (and pays the 5% referrer commission).
- **Toast feedback** on notifications, search, filters, history, payout methods,
  profile menu items and logout.

## Notes

- **Demo data, mostly.** `FakeRepository` holds sample tasks, balances and
  transactions. Auth is real (Supabase), and the referral system + earnings
  ledger (including withdrawals) are real — the SQL files in `supabase/` are
  already deployed. Task catalog content itself is still sample data.
- **TODO markers** in the code flag the natural next steps: task details,
  notifications, search/filters, settings.
- Social buttons (Google/Apple) are UI placeholders — connect real OAuth later.
