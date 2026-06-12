# Signature Mobile — Firebase Analytics Event Sequence

> Reference for validating events in **Firebase DebugView**.
> Covers every automatic and custom event fired during app launch and the full login flow.
>
> Enable DebugView on device before testing:
>
> **Debug builds (shared APK / IPA):** DebugView is automatically enabled — no ADB or
> scheme args required. Android sets `firebase_analytics_debug_mode=true` via
> `manifestPlaceholders` in `build.gradle.kts`; iOS sets the `UserDefaults` debug key
> in `iOSApp.swift` inside a `#if DEBUG` block.
>
> **Release builds or manual override:**
> - **Android:** `adb shell setprop debug.firebase.analytics.app com.siteflow.signature`
>   (restart the app after setting — the prop is only read at startup)
> - **iOS:** launch scheme arg `-FIRAnalyticsDebugEnabled` in Xcode scheme
>
> After enabling, select your device in the **Firebase Console → Analytics → DebugView**
> device dropdown — events will not appear until the correct device is selected.

---

## Scenario A — Cold Start, Not Logged In (Fresh Install)

### Flow Overview

```
T0  MainActivity.onCreate()
T1  MainActivity.onStart()
T2  Compose renders → NavHost resolves → Login route active
T3  User types mobile number + taps Submit
T4  API success → navigate to OTP screen
T5  OtpScreen enters composition
T6  User fills all 6 digits (auto-submit triggers)
T7  OTP verified → navigate to role dashboard
```

---

### T0 — `MainActivity.onCreate()`

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 1 | `first_open` | **Auto (Firebase)** | Firebase SDK | — first install only |
| 2 | `app_opened` | Custom | `MainActivity.kt:59` | `platform=android`, `os_version`, `app_version`, `build_number`, `is_fresh_install=true` |

> `is_fresh_install` is read from `SharedPreferences("analytics_prefs")` and flipped to `false`
> immediately after — it is `true` exactly once per install.

---

### T1 — `MainActivity.onStart()`

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 3 | `session_start` | **Auto (Firebase)** | Firebase SDK | — Firebase's own session bookkeeping |
| 4 | `session_started` | Custom | `MainActivity.kt:77` | `user_role=""`, `user_id=""`, `business_id=""`, `is_returning_user=false` |

> Both `session_start` (Firebase auto) and `session_started` (custom) fire here.
> IDs are empty at this point — no token exists yet. Identity is set later at T7.

---

### T2 — NavHost renders → Login route active

`AppStartViewModel.resolveStartDestination()` finds no token → `startDestination = "login"`.

`AppNavHost` `LaunchedEffect(currentRoute)` fires:

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 5 | `screen_view` | **Auto (Firebase)** | Firebase SDK | Activity name only — no Compose screen name |
| 6 | `screen_viewed` | Custom | `AppNavHost.kt:118` | `screen_name="Login"`, `previous_screen="unknown"`, `user_role="UNKNOWN"` |
| 7 | `login_screen_viewed` | Custom | `AppNavHost.kt:128` | `is_auto_redirect=false` |

> Events **#6 and #7 fire in the same `LaunchedEffect` block**, back-to-back.

---

### T3 — User types mobile number, taps Submit

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 8 | `login_submitted` | Custom | `LoginViewModel.kt:36` | `mobile_number_length=10` |

> Fires **before** the API call.
> If the API fails → `login_failed` fires instead (see [Error Paths](#error--recovery-paths)).

---

### T4 — API returns success → navigate to `VerifyOtp` route

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 9 | `screen_viewed` | Custom | `AppNavHost.kt:118` | `screen_name="OTP Verification"`, `previous_screen="Login"`, `user_role="UNKNOWN"` |

> `otp_screen_viewed` is intentionally **not** fired here. It needs the masked phone number,
> which only `OtpViewModel.setMobileNumber()` has.

---

### T5 — `OtpScreen` enters composition

`OtpScreen`'s `LaunchedEffect(mobileNumber)` calls `viewModel.setMobileNumber(mobileNumber)`:

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 10 | `otp_screen_viewed` | Custom | `OtpViewModel.kt:48` | `mobile_number_masked="XX******XX"` |

> Fires **after** `screen_viewed` (#9) because it's in the composable's `LaunchedEffect`,
> not the NavHost. Expect a **100–300 ms gap** between #9 and #10 in DebugView.

---

### T6 — User enters all 6 OTP digits

Auto-submit triggers via `LaunchedEffect(state.isValid)` in `OtpScreen`:

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 11 | `otp_entered` | Custom | `OtpViewModel.kt:99` | `time_to_enter_ms=0` |

> Fires at the **start** of `submit()`, before the verify API call.

---

### T7 — OTP API returns success

The richest moment in the flow. Events fire in this exact order inside `OtpViewModel.submit().onSuccess`:

| # | Event / Call | Type | Source | Detail |
|---|---|---|---|---|
| — | `analytics.identify(businessId)` | Identity | `OtpViewModel.kt:145` | Sets Firebase User ID — not a tracked event |
| — | `analytics.setUserProperty(...)` × 6 | Property | `OtpViewModel.kt:146–151` | Sets `user_role`, `business_id`, `app_version`, `platform`, `os_version`, `device_model` |
| 12 | `otp_verified` | Custom | `OtpViewModel.kt:152` | `user_role="ASE"`, `time_to_verify_ms=0`, `is_first_login=false` |

> `identify()` and `setUserProperty()` run **before** `otp_verified` is tracked.
> All events after this point carry the user identity in Firebase.

---

### T8 — Navigate to role dashboard

| # | Event Name | Type | Source | Key Params |
|---|---|---|---|---|
| 13 | `screen_viewed` | Custom | `AppNavHost.kt:118` | `screen_name="ASE Home"` *(or `ASM Home` / `Outlet Dashboard`)*, `previous_screen="OTP Verification"`, `user_role="ASE"` |

---

## Complete DebugView Checklist — Scenario A

```
 1.  first_open             [AUTO]    ← first install only
 2.  app_opened             [CUSTOM]  is_fresh_install=true
 3.  session_start          [AUTO]
 4.  session_started        [CUSTOM]  empty IDs (pre-login)
 5.  screen_view            [AUTO]    ← Activity level only
 6.  screen_viewed          [CUSTOM]  screen_name=Login
 7.  login_screen_viewed    [CUSTOM]  is_auto_redirect=false
 8.  login_submitted        [CUSTOM]  mobile_number_length=10
 9.  screen_viewed          [CUSTOM]  screen_name=OTP Verification
10.  otp_screen_viewed      [CUSTOM]  mobile_number_masked=XX******XX  (~100–300ms after #9)
11.  otp_entered            [CUSTOM]  time_to_enter_ms=0
     ── identify() + setUserProperty() × 6 ──────────────────────────
12.  otp_verified           [CUSTOM]  user_role=ASE
13.  screen_viewed          [CUSTOM]  screen_name=ASE Home
```

---

## Scenario B — Returning User (Already Logged In)

`AppStartViewModel` finds a valid token and routes directly to the dashboard.
The entire login funnel (events #6–12 above) is skipped.

```
 1.  app_opened             [CUSTOM]  is_fresh_install=false
 2.  session_start          [AUTO]
 3.  session_started        [CUSTOM]  user_id=<businessId>, is_returning_user=true
 4.  screen_view            [AUTO]
 5.  screen_viewed          [CUSTOM]  screen_name=ASE Home (or role dashboard)
```

---

## Error / Recovery Paths

| User Action | Event Fired | Source | Key Params |
|---|---|---|---|
| Login API fails | `login_failed` | `LoginViewModel.kt:49` | `error_message`, `error_code` |
| OTP wrong / expired | `otp_verification_failed` | `OtpViewModel.kt:183` | `error_message`, `attempt_number=1` |
| Taps Resend OTP | `otp_resend_tapped` | `OtpViewModel.kt:196` | `resend_attempt_number`, `countdown_remaining` |
| Taps Edit Number | `otp_edit_number_tapped` | `OtpViewModel.kt:211` | — |
| App goes to background | `app_backgrounded` | `MainActivity.onPause()` | — |
| App returns to foreground | `session_started` fires again | `MainActivity.onStart()` | `is_returning_user=true` |
| User logs out (taps Logout) | `logout_tapped` | `ProfileViewModel` | `user_role`, `screen` |
| Logout confirmed | `logout_confirmed` | `ProfileViewModel.logout()` | `session_duration_ms` |

---

## Known Param Gaps (values currently `0` or hardcoded)

| Event | Param | Current Value | Fix Needed |
|---|---|---|---|
| `otp_entered` | `time_to_enter_ms` | `0L` | Wire `kotlinx.datetime` clock from OTP screen entry |
| `otp_verified` | `time_to_verify_ms` | `0L` | Wire clock from OTP submit call start |
| `otp_verified` | `is_first_login` | `false` | Derive from server response or local flag |
| `session_started` (T1) | `user_id`, `business_id` | `""` | Expected — identity not yet set at `onStart()` pre-login |

---

## Auto vs Custom Event Summary

| Firebase Auto Events | Fired By | When |
|---|---|---|
| `first_open` | Firebase SDK | First ever install |
| `app_update` | Firebase SDK | App version changes |
| `session_start` | Firebase SDK | New session begins |
| `screen_view` | Firebase SDK | Activity resumes (Compose = one Activity, fires once) |
| `user_engagement` | Firebase SDK | App in foreground for engagement threshold |

| Custom Events (this project) | Count in login flow |
|---|---|
| App lifecycle | 3 (`app_opened`, `session_started`, `app_backgrounded`) |
| Navigation | 3× `screen_viewed` (Login → OTP → Dashboard) |
| Login funnel | 4 (`login_screen_viewed`, `login_submitted`, `otp_screen_viewed`, `otp_entered`, `otp_verified`) |
| **Total happy-path custom events** | **13** |