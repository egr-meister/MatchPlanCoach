# MatchPlan Coach

A native Android app for **planning amateur football matches**. Built with
Kotlin, Jetpack Compose and Material 3. Portrait-only, English UI, no account,
no ads, no analytics, no payments.

MatchPlan Coach helps you plan your next match: add the opponent, date, time and
venue, build a player list, pick a starting lineup and formation, add match
tasks, and keep pre- and post-match notes. A small, secondary **Match Schedule**
screen can optionally show football fixtures from the football-data.org API v4
as an extra reference — but match planning is the core of the app.

---

## 1. Project description

MatchPlan Coach is a manual, local-first planning board for an amateur football
team. Everything you create — matches, players, squads, lineups, tasks and notes
— is entered by you and stored on your device. The app never claims to be an
official football product and does not give professional coaching advice.

## 2. Features

- Create, edit, delete and duplicate future match plans
- Opponent, match date/time, venue, match type and status
- Local player list with number, position and availability
- Match squad selection (starters, bench, unavailable)
- Starting lineup on a top-view pitch with 5 formations and tap-to-assign
- Match tasks with categories and completion tracking
- Match notes: pre-match, tactical, player, post-match and result notes
- Match history with status filtering
- A **secondary** Match Schedule screen (football-data.org API v4)
- Match schedule settings, caching and demo-data fallback
- Full local data management and reset in Settings

## 3. Match planning disclaimer

> MatchPlan Coach is a manual football match planning app for amateur teams.
> Matches, players, lineups, tasks, and notes are created by the user. The app
> is not an official football tool and does not provide professional coaching
> advice.

This note appears in onboarding, in Settings, and here in the README.

## 4. Match schedule API disclaimer

> Match data is provided by football-data.org. Availability, accuracy,
> competitions, and update frequency depend on the API provider and the current
> API plan.

This note appears on the Match Schedule screen, in Settings, and here.

## 5. football-data.org API v4

The Match Schedule feature uses only the football-data.org v4 `/matches`
endpoint:

```
Base URL: https://api.football-data.org/v4
Endpoint: GET /matches?dateFrom=<from>&dateTo=<to>
Header:   X-Auth-Token: <your token>
```

No odds, predictions, bookmaker, betting or streaming endpoints are ever called.

## 6. Default 10-day API window

By default the Match Schedule requests matches from **today** through
**today + 9 days** (a 10-day window). Dates are computed locally from the device
date and formatted as `YYYY-MM-DD`:

```
GET /matches?dateFrom=<today>&dateTo=<todayPlus9Days>
```

Example: if today is `2026-07-08`, the app requests
`GET /matches?dateFrom=2026-07-08&dateTo=2026-07-17`.

You can override the window in Match Schedule settings; leaving both dates empty
restores the default 10-day window.

## 7. local.properties setup

The API token is read from `local.properties` at build time and exposed through
`BuildConfig`. It is **never** hardcoded in source.

1. Copy `local.properties.example` to `local.properties`.
2. Fill in your token (get a free key at football-data.org):

```properties
FOOTBALL_DATA_API_TOKEN=your_real_token_here
FOOTBALL_API_BASE_URL=https://api.football-data.org/v4
```

`BuildConfig.FOOTBALL_DATA_API_TOKEN` and `BuildConfig.FOOTBALL_API_BASE_URL`
are generated from these values.

## 8. Never commit API tokens

`local.properties` is listed in `.gitignore` and must never be committed. The
real token must not appear in source, README, screenshots, tests or CI logs. For
CI, provide it via the optional `FOOTBALL_DATA_API_TOKEN` GitHub Secret.

## 9. No ads / no analytics / no payments

The app contains no advertising SDKs, no analytics SDKs and no payment SDKs. It
does not track you and has no in-app purchases.

## 10. No betting / no odds / no bookmakers

There is no betting, gambling or casino functionality. No odds, bookmakers,
predictions, coins, cash, prizes, jackpots, deposits, balances or free bets.

## 11. No official logos

The app uses no official club or league logos. Team and competition names shown
on the Match Schedule screen are plain text returned by the API only.

## 12. No copied reference branding

The visual style ("Blue Green Match Planner") uses only a navy/green/white
colour mood. No reference logo, silhouette or brand identity is copied.

## 13. No account registration

There is no sign-up, login or account of any kind.

## 14. Local storage

All user data (match plans, players, squads, lineups, tasks, notes, settings and
the cached match schedule) is stored locally on the device only. There is no
backend created by the app and no cloud sync.

## 15. DataStore

Local storage uses **DataStore Preferences**. The entire app state is serialized
to a single JSON string with Kotlinx Serialization. Decoding is defensive:
empty storage, missing fields and corrupted JSON all fall back to safe defaults,
so the app never crashes on load.

## 16. Internet usage

The internet is used only by the Match Schedule feature to call
football-data.org. Match planning works fully offline.

## 17. INTERNET permission

The manifest declares only `android.permission.INTERNET`, used solely for the
secondary Match Schedule API.

## 18. No location permission
The app never requests or uses location.

## 19. No notification permission
The app never requests notifications and never sends push notifications.

## 20. No sensors
No body sensors, activity recognition or physical-activity sensors are used.

## 21. No Google Fit
The app does not integrate Google Fit.

## 22. No Health Connect
The app does not integrate Health Connect.

## 23. No wearable integration
The app has no wearable/companion integration.

## 24. No automatic tracking
The app performs no automatic activity tracking of any kind.

## 25. Match planning
Create future matches with opponent, date, time, venue, type, status, team name
and general notes. Validation is friendly and never crashes on bad input.

## 26. Player list
Maintain a local roster with name, optional number (1–99), position and
availability. Players can be added, edited and deleted.

## 27. Match squad
For each match, mark players as starters, bench or unavailable. Deleting a
player safely removes them from all squads and lineups.

## 28. Starting lineup
Choose a formation (4-4-2, 4-3-3, 3-5-2, 5-3-2, 4-2-3-1) and assign players to
fixed pitch slots with a simple **tap-to-assign** flow (no drag-and-drop). Tap a
filled slot to Replace or Clear.

## 29. Match tasks
Add tasks with categories (Attack, Defense, Pressing, Set Pieces, Fitness, Team
Talk, Equipment, Custom), mark them complete, and track progress.

## 30. Match notes
Keep pre-match, tactical, player, post-match and result notes per match.

## 31. Match Schedule (secondary feature)
An optional reference screen that lists fixtures from football-data.org for the
active date window. It is intentionally secondary and does not define the app.

## 32. Match cache
The latest successful schedule response is cached locally (with last-updated
time and the requested date window) and shown on next open.

## 33. Demo data fallback
If no API token is configured (or demo mode is on), the app shows built-in demo
matches with generic team names. It also falls back to cache or demo data on any
API failure.

## 34. App icon concept
A custom adaptive icon: a navy rounded-square background, a green football-field
ring, a white player/ball marker and a small green check — a clean amateur match
planner mark. No official logos, real player photos or betting symbols. Adaptive
vector for API 26+ plus PNG fallbacks for API 24–25.

## 35. Splash screen concept
A custom splash (via `androidx.core:core-splashscreen`): navy background with the
centered white/green match-planner icon. No default Android splash, no heavy
assets.

## 36. Visual style concept
Sporty, clean and practical. White content cards, calendar-style date badges,
player chips, a green pitch board and calm secondary sections.

## 37. "Blue Green Match Planner" visual explanation
Navy blue is the main identity colour; green is the football-field accent; white
cards hold readable content. Yellow is used only for warnings/doubtful
availability and red only for errors/unavailable status. No casino gold, no neon
glow, no heavy gradients, no betting-slip UI.

## 38. Layout uniqueness
The app deliberately avoids the generic "mascot → title → subtitle → stat card →
stack of big buttons → settings" template. The home screen is built around match
cards, a next-match highlight, a quick-action grid and a small schedule teaser.

## 39. Open the project in Android Studio
1. Install Android Studio (Koala or newer).
2. `File → Open` and select the project root (this folder).
3. Let Gradle sync. Android Studio will download the Gradle wrapper and SDK
   components as needed.
4. Create `local.properties` (see section 7) if you want live match data.
5. Run the `app` configuration on a device/emulator (API 24+).

## 40. Configure the API token in local.properties
See section 7. Without a token the app still runs and shows demo matches.

## 41. Build Android
From the project root (after `local.properties` exists):

```bash
# Debug
./gradlew assembleDebug

# Release APK + AAB (requires signing config, see section 42–43)
./gradlew assembleRelease bundleRelease
```

> If the Gradle wrapper JAR is not present yet, run `gradle wrapper
> --gradle-version 8.9` once (or just open the project in Android Studio, which
> generates it automatically). CI regenerates the wrapper as well.

## 42. Generate a PKCS12 keystore
Release builds must be signed with a real PKCS12 keystore (not a debug key):

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore matchplan-coach-release-key.p12 \
  -alias matchplan_coach_key \
  -keyalg RSA -keysize 2048 -validity 10000
```

Use the **same password** for the keystore and the key. Never commit the
keystore or its passwords.

For local release builds you can create a gitignored `keystore.properties` in the
project root:

```properties
storeFile=/absolute/path/to/matchplan-coach-release-key.p12
storePassword=your_password
keyAlias=matchplan_coach_key
keyPassword=your_password
```

If no signing config is provided locally, the release build falls back to the
debug signer so `assembleRelease` still runs — but CI always uses the real
keystore.

## 43. Add GitHub Secrets
In your repository, add these secrets (Settings → Secrets and variables →
Actions):

- `ANDROID_KEYSTORE_BASE64` — base64 of your `.p12` (`base64 -w0 key.p12`)
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `FOOTBALL_DATA_API_TOKEN` — optional; build succeeds without it (demo data)

## 44. GitHub Actions
`.github/workflows/android-build.yml` runs on push to `main`. It:

1. Sets up JDK 17 and the Android SDK.
2. Installs `platforms;android-35` and `build-tools;35.0.0`.
3. Creates `local.properties` from secrets (token hidden; placeholder if absent).
4. Decodes the release keystore from `ANDROID_KEYSTORE_BASE64`.
5. Builds the **signed release APK and AAB**.
6. Verifies the APK signature with `apksigner verify --print-certs` and **fails**
   if the certificate contains `CN=Android Debug`.
7. Uploads the APK and AAB as workflow artifacts.

No emulator smoke test runs in CI (see section 49 for local launch verification).

## 45. Google Play compatibility notes
- Upload the **.aab** (App Bundle), not the APK.
- Release artifacts must be signed with a real (non-debug) certificate; CI
  enforces this.
- Targets API 35.

## 46. Android API 35 notes
`compileSdk = 35`, `targetSdk = 35`, `minSdk = 24`. Uses current stable Android
Gradle Plugin and Kotlin.

## 47. 16 KB page size compatibility
The app ships **no native libraries**, so it is compatible with Android 15+/16 KB
memory page sizes. `jniLibs.useLegacyPackaging = false` is set.

## 48. Release optimization notes
R8/shrinking is configured with `proguard-rules.pro` (keep rules for
kotlinx.serialization, Retrofit and OkHttp). **Recommended flow:** first verify a
non-minified release build:

```kotlin
// app/build.gradle.kts -> buildTypes.release
isMinifyEnabled = false
isShrinkResources = false
```

Confirm the app launches, then set both back to `true` (the committed default)
and re-test.

## 49. Local launch verification checklist
CI proving it *builds* is not proof it *launches*. Before release, install the
release APK and check logcat for crashes:

```bash
adb install app/build/outputs/apk/release/app-release.apk
adb logcat | grep -i "AndroidRuntime\|matchplan"
```

Verify there are no `ClassNotFoundException`, `NoSuchMethodError`, serialization
or DataStore-parse crashes, and walk through:

- first launch with empty storage; complete onboarding
- create/edit/duplicate/delete a match plan
- add/edit/delete a player
- build a squad; pick a formation; assign and clear lineup slots
- add/complete/delete tasks; write pre- and post-match notes
- open Match Schedule with and without a token
- confirm the default request uses today + 9 days
- manual refresh; simulate API failure; check cached/demo fallback
- clear match cache; reset all local data; relaunch; airplane-mode launch
- verify the release APK signature; confirm only INTERNET permission is present

## 50. Privacy note

> MatchPlan Coach stores match plans, player lists, lineups, tasks, notes,
> settings, and cached match data on this device. The app uses internet only to
> load football match data from football-data.org. No account, no ads, no
> analytics, no payments, no Firebase, no location, no notifications, no sensors,
> no Google Fit, and no Health Connect.

---

### Project structure

```
app/src/main/java/com/matchplan/coach/
├── MainActivity.kt                 # Splash + Compose host
├── MatchPlanApplication.kt         # Repository container
├── data/
│   ├── model/                      # Data classes, enums, formations
│   ├── local/                      # DataStore, demo data
│   ├── remote/                     # football-data.org service + repo + DTOs
│   └── repository/                 # AppRepository (local CRUD)
├── ui/
│   ├── AppViewModel.kt             # Shared ViewModel + schedule state
│   ├── theme/                      # Blue Green Match Planner theme
│   ├── components/                 # Reusable Compose components
│   ├── navigation/                 # Routes + NavHost
│   └── screens/                    # All 15 screens
└── util/DateUtils.kt               # Safe date/time helpers
```

Technology: Kotlin · Jetpack Compose · Material 3 · Navigation Compose ·
Coroutines · ViewModel · DataStore Preferences · Kotlinx Serialization ·
Retrofit · OkHttp · Gradle Kotlin DSL.
