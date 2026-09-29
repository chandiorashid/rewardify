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

## App logic included

- **Login/Signup validation** — empty-field, email-format, password-length and
  password-match checks with toast feedback; forgot-password and social buttons
  show contextual toasts.
- **Withdraw dialog** — amount entry with validation (valid number, $5.00 minimum,
  sufficient balance) and a success toast on confirmation. Available on Home and Wallet.
- **Gift-card redemption dialog** — code validation with a success toast.
- **Tasks** — category tabs filter the list; tapping a Home category tile opens
  Tasks pre-filtered to that category; Start/Continue buttons show toasts.
- **Toast feedback** on notifications, search, filters, history, payout methods,
  profile menu items and logout.

## Notes

- **Demo data only.** `FakeRepository` holds sample tasks, balances and transactions.
  Login/Signup accept any input and jump straight into the app. Wire these to a
  real backend (e.g. Retrofit + your API) when ready.
- **TODO markers** in the code flag the natural next steps: task details,
  withdraw flow, payout methods, notifications, search/filters, settings.
- Social buttons (Google/Apple) are UI placeholders — connect real OAuth later.
