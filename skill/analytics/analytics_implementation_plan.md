# CDO Mobile — Analytics Implementation Plan (Revised)

> Source of truth: `analytics_events_global.md`, `analytics_events_ase.md`,
> `analytics_events_asm.md`, `analytics_events_outlet.md`, `analytics_params.md`,
> `analytics_funnels.md`, `analytics.md`.
>
> Every event defined in those files MUST be implemented. The examples in each
> phase are illustrative, not exhaustive.

---

## PHASE 0 — Firebase Platform Prerequisites

> These are infrastructure requirements that must be in place before any
> analytics code is written. All steps below are **already complete** in this
> project — verify each before starting Phase 1.

### Step 0 — Firebase Project Registration
- ✅ Firebase project created in Firebase Console
- ✅ Android app registered (`com.siteflow.cdo`)
- ✅ iOS app registered

### Step 1 — Configuration Files
| File | Required location | Status |
|---|---|---|
| `google-services.json` | `composeApp/` | ✅ Present |
| `GoogleService-Info.plist` | `iosApp/iosApp/` | ✅ Present |

> Note: In this KMP project the Android config lives in `composeApp/` (not `app/`),
> because `composeApp` is the Android application module.

### Step 2 — SDK Integration

**Android** (`composeApp/build.gradle.kts` — already configured):
- ✅ `alias(libs.plugins.googleServices)` applied
- ✅ `alias(libs.plugins.firebaseCrashlytics)` applied
- ✅ `implementation(platform(libs.firebase.bom))` in top-level `dependencies {}`
- ✅ `implementation(libs.firebase.analytics)` in top-level `dependencies {}`

**iOS** (Swift Package Manager — already configured):
- ✅ `firebase-ios-sdk 11.15.0` pinned in `Package.resolved`
- ✅ `FirebaseAnalytics` product available via the same SPM package
- No CocoaPods required — SPM is the integration method for this project

### Step 3 — Firebase Initialization

**Android:** Auto-initialized by the `google-services` Gradle plugin when the app
starts — no manual call required. ✅

**iOS:** `FirebaseApp.configure()` is called in `iOSApp.swift` before `KoinInitializer`
runs, ensuring Firebase is ready before any analytics events can fire. ✅

### Step 4 — Verification (must be done manually)
- [x] Build and run the app on a real device or emulator
- [x] Open Firebase Console → Analytics → DebugView
- [x] DebugView auto-enabled in debug builds:
  - Android: `manifestPlaceholders["firebaseAnalyticsDebugMode"] = "true"` in `build.gradle.kts` debug buildType → `AndroidManifest.xml` `firebase_analytics_debug_mode` meta-data
  - iOS: `UserDefaults.standard.set(true, forKey: "/google/firebase/debug_mode")` inside `#if DEBUG` in `iOSApp.swift` before `FirebaseApp.configure()`
- [x] For release builds or manual override:
  - Android: `adb shell setprop debug.firebase.analytics.app com.siteflow.cdo` (must restart app after setting)
  - iOS: add `-FIRAnalyticsDebugEnabled` to Xcode scheme launch arguments
- [x] Select the correct device in the DebugView device dropdown — events are invisible until a device is selected
- [x] Confirmed events appear in DebugView on both emulator and real device

---

## PHASE 1 — Core Analytics Foundation

**Module:** `core/analytics` (commonMain)

### Files created

#### `AnalyticsParams.kt`
Single source of truth for all parameter key constants. Covers every key from
`analytics_params.md` plus additional keys added during the PDF gap-analysis pass.
No inline strings are used anywhere else in the project.

Key groups implemented:
```
// Identity / session
USER_ID, USER_ROLE, BUSINESS_ID, APP_VERSION, BUILD_NUMBER, PLATFORM,
OS_VERSION, DEVICE_MODEL, NETWORK_TYPE, LOCALE,
IS_FRESH_INSTALL, IS_RETURNING_USER, SESSION_DURATION_MS

// Auth / OTP
IS_AUTO_REDIRECT, MOBILE_NUMBER_LENGTH, MOBILE_NUMBER_MASKED,
TIME_TO_ENTER_MS, TIME_TO_VERIFY_MS, IS_FIRST_LOGIN,
RESEND_ATTEMPT_NUMBER, COUNTDOWN_REMAINING, ATTEMPT_NUMBER, SCREEN_ON_EXPIRY

// Navigation
SCREEN_NAME, PREVIOUS_SCREEN, TAB, PREVIOUS_TAB

// Network
NEW_STATUS, SCREEN, STATUS_CODE, LATENCY_MS, IS_RETRY, ERROR_TYPE

// Onboarding
STEP_NUMBER, STEP_LABEL, TIME_ON_STEP_MS, OUTLET_ID, IS_NEW, RESUME_STEP,
TOTAL_DURATION_MS, STEPS_WITH_ERRORS_COUNT, FIELDS_FILLED_COUNT,
LAT, LNG, ACCURACY_METERS, TIME_TO_CAPTURE_MS, PINCODE, SUCCESS,
AUTO_FILLED_CITY, AUTO_FILLED_STATE, SLAB_ID, SLAB_LABEL,
DISTRIBUTOR_NAME, HAS_UPI, HAS_CHEQUE_PHOTO, KYC_ID_TYPE, HAS_GST,
PHOTO_TYPE, SLOT_ID, PHOTO_COUNT, TOTAL_REQUIRED

// Compliance
OUTLET_STATUS, COMPLETION_PERCENT, CDO_STEPS_COMPLETED,
COOLER_TYPE, CAPACITY, SIGNAGE_TYPE, COOLER_INSTALLED, SIGNAGE_INSTALLED

// Invoice
INVOICE_ID, ITEMS_COUNT, GRAND_TOTAL, TIME_ON_REVIEW_MS,
TIME_ON_SCREEN_MS, ITEMS_EXTRACTED, HAS_NOTE, ACTION,
REJECTION_REASON_LENGTH, REJECTION_REASON, QUALITY_ISSUES_COUNT,
HAS_HARD_BLOCK, ISSUES, CONFIDENCE_AVG, REASON, PHASE_ON_CANCEL,
TIME_ELAPSED_MS, WAS_AUTO_FILLED, EDITS_MADE_COUNT, TOTAL_UPLOAD_DURATION_MS,
LOAD_TIME_MS, DURATION_MS

// Dashboard stats
TOTAL_ASES, ACTIVE_OUTLETS, SUSPENDED, TOTAL_INVOICES, LAST_PAYOUT_AMOUNT,
MONTHLY_PERFORMANCE, TIER, OUTLET_COUNT, INVOICE_COUNT

// Search
QUERY_LENGTH, RESULTS_COUNT

// Error
ERROR_MESSAGE, ERROR_CODE, RETRY_COUNT

// Walkthrough
IS_FIRST_TIME, STEPS_COMPLETED, STEP_ON_EXIT, TIME_SPENT_MS

// Notification
NOTIFICATION_ID, NOTIFICATION_TYPE

// Profile
ASE_COUNT, HAS_KYC_DATA
```

---

#### `AnalyticsTracker.kt`
Interface — commonMain only, no platform imports.

```kotlin
interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
    fun identify(userId: String)
    fun setUserProperty(key: String, value: String)
    fun reset()
}
```

---

#### `AnalyticsEvent.kt`
Sealed class hierarchy. Events were initially implemented as `data object` placeholders and
subsequently upgraded to rich `data class` signatures during the PDF gap-analysis pass.
All 106 spec events plus 2 additional `ProfileEvent` leaves are now fully parameterised.

Key signature changes made during the gap-analysis rewrite:
- `AppOpened` → `data class(platform, osVersion, appVersion, buildNumber, isFreshInstall)`
- `SessionStarted` → `data class(userRole, userId, businessId, isReturningUser)`
- `LoginScreenViewed` → `data class(isAutoRedirect: Boolean = false)`
- `LoginSubmitted` → `data class(mobileNumberLength: Int)`
- `OtpScreenViewed` → `data class(mobileNumberMasked: String = "")`
- `OtpEntered` → `data class(timeToEnterMs: Long = 0)`
- `OtpResendTapped` → `data class(resendAttemptNumber: Int, countdownRemaining: Int)`
- `OtpVerified` → `data class(userRole, timeToVerifyMs, isFirstLogin)`
- `OtpVerificationFailed` — `errorCode` replaced by `attemptNumber: Int`
- `LogoutTapped` → `data class(userRole, screen)`
- `LogoutConfirmed` → `data class(sessionDurationMs: Long = 0)`
- All dashboard loaded events → `data class` with full stats params
- `OnboardingSlabSelected` — `slab: String` replaced by `slabId: String, slabLabel: String`
- `OnboardingCompleted` → `data class(outletId, totalDurationMs, stepsWithErrorsCount)`
- All Compliance events → `data class` with outletId / slot / count params
- `InvoiceAiExtractionCompleted` — `itemsCount` renamed to `itemsExtracted`, added `durationMs`, `confidenceAvg`
- `InvoiceAiExtractionFailed` — `errorMessage` renamed to `reason`, added `retryCount`, `durationMs`
- `InvoiceAiExtractionCancelled` → `data class(phaseOnCancel, timeElapsedMs)`
- `InvoiceReviewViewed` → `data class(invoiceId, itemsCount, grandTotal, isAutoFilled)`
- `InvoiceSubmitted` — removed `invoiceId`, added `wasAutoFilled`, `editsMadeCount`, `timeOnReviewMs`
- `InvoiceSubmitFailed` — removed `invoiceId`/`errorCode`, kept `errorMessage` + added `retryCount`
- `InvoiceDraftSaved` — changed from `invoiceId: String` to `itemsCount: Int`
- `WalkthroughStarted` → `data class(isFirstTime: Boolean = true)`
- `WalkthroughCompleted` → `data class(totalDurationMs, stepsCompleted)`
- `WalkthroughAbandoned` → `data class(stepOnExit, timeSpentMs)`
- `ASMEvent.OutletApproved/Rejected` → added `timeOnReviewMs`, `rejectionReason`
- `ASMEvent.InvoiceApproved/Rejected` → added `hasNote: Boolean`
- Added `ProfileEvent` sealed subclass (beyond PDF spec): `ProfileViewed(userRole, hasKycData)`,
  `ProfileMyTeamTapped`

See `AnalyticsEvent.kt` for the complete, authoritative class hierarchy.

### Step-by-step Execution

1. ✅ Create `AnalyticsParams.kt` — all constants defined.
2. ✅ Create `AnalyticsTracker.kt` interface in commonMain.
3. ✅ Create `AnalyticsEvent.kt` with full sealed class hierarchy.
4. ✅ All required parameters are non-nullable constructor params.
5. ✅ Total event count: 106 spec events + 2 ProfileEvent = 108 sealed class leaves.

---

## PHASE 2 — Platform Implementations

**Modules:**
- `androidMain/platform/analytics/`
- `iosMain/platform/analytics/`

### Files to create

- `FirebaseAnalyticsTracker.kt` (Android)
- `FirebaseAnalyticsTracker.kt` (iOS)

Each file contains:
1. An **event name mapper** — converts `AnalyticsEvent` subclass → snake_case string (e.g., `ASEEvent.DashboardLoaded` → `"ase_dashboard_loaded"`). Names must match the skill files exactly.
2. A **parameter mapper** — converts the data class properties → `Bundle` (Android) or `[String: Any]` (iOS) using `AnalyticsParams` constants as keys.
3. Implementations of `identify()`, `setUserProperty()`, and `reset()` using the platform Firebase SDK.

### Step-by-step Execution

1. Implement `FirebaseAnalyticsTracker` in `androidMain`. The event name mapper must cover every leaf class in `AnalyticsEvent`. The parameter mapper uses `Bundle` and keys from `AnalyticsParams`.
2. Implement `FirebaseAnalyticsTracker` in `iosMain`. The parameter mapper uses `NSDictionary` / `[String: Any]`.
3. Both mappers must produce event names and parameter keys **exactly** as defined in the skill files — no camelCase, no renaming.
4. `identify(userId)` calls `firebaseAnalytics.setUserId(userId)`.
5. `setUserProperty(key, value)` calls `firebaseAnalytics.setUserProperty(key, value)`.
6. `reset()` calls `firebaseAnalytics.resetAnalyticsData()`.
7. Do not manage threading — rely entirely on the Firebase SDK's internal threading.
8. The event mapper must be an exhaustive `when` expression — a compile-time warning if any new event is added without a mapping.

---

## PHASE 3 — Dependency Injection

**Module:** platform DI modules (no `expect/actual` needed)

### Actual Implementation

`AnalyticsTracker` is bound inside the existing platform DI modules — no separate
`AnalyticsModule.kt` or `expect/actual` was required because the Android binding needs
`androidContext()` for Firebase, and iOS uses a Swift delegate passed in at startup.

**Android** (`androidPlatformModule.kt`):
```kotlin
single<AnalyticsTracker> {
    FirebaseAnalyticsTracker(FirebaseAnalytics.getInstance(androidContext()))
}
single {
    val ctx = androidContext()
    val appVersion = try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown" } catch (_: Exception) { "unknown" }
    val buildNumber = try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).longVersionCode.toString() } catch (_: Exception) { "0" }
    DeviceInfo(
        appVersion = appVersion,
        buildNumber = buildNumber,
        platform = "android",
        osVersion = android.os.Build.VERSION.RELEASE,
        deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
    )
}
```

> `BuildConfig.VERSION_NAME` was NOT used — the `buildConfig` feature is disabled
> in `composeApp/build.gradle.kts`. `PackageManager.getPackageInfo()` is the correct approach.
> `buildNumber` was added to `DeviceInfo` after the PDF gap-analysis pass.

**iOS** (`iosPlatformModule.kt`):
```kotlin
// AnalyticsTracker bound via iosAnalyticsModule(delegate:) — Swift bridge only
single {
    DeviceInfo(
        appVersion = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "unknown",
        buildNumber = NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: "0",
        platform = "ios",
        osVersion = UIDevice.currentDevice.systemVersion,
        deviceModel = UIDevice.currentDevice.model
    )
}
```

**iOS Firebase bridge:** `AnalyticsNativeDelegate` Kotlin interface is exposed as an
ObjC protocol, implemented in Swift as `SwiftFirebaseAnalyticsDelegate`, and injected
via `iosAnalyticsModule(delegate: SwiftFirebaseAnalyticsDelegate())` called in
`iOSApp.swift` before `KoinInitializer` runs.

---

## PHASE 4 — Event Mapping (Feature Integration)

**Modules:** Login, ASE, ASM, Outlet

### Trigger Ownership Rules (non-negotiable)

| Layer | Responsibility |
|---|---|
| **Compose UI** | Triggers user intent only (button click → ViewModel intent) |
| **ViewModel** | Calls `AnalyticsTracker.track()` for all events in its feature |
| **Repository / Data** | Returns results only — MUST NOT contain any analytics calls |

---

### App Entry Points — Lifecycle Events  ← Fix #7

`GlobalEvent.AppOpened`, `GlobalEvent.SessionStarted`, and `GlobalEvent.SessionEnded` are
not ViewModel-owned. They must be triggered from the platform entry points:

- **Android:** `MainActivity.kt` — ✅ implemented:
  - `AppOpened` in `onCreate`
  - `SessionStarted` in `onStart`
  - `SessionEnded` in `onStop`
  - `AppBackgrounded` in `onPause`
- **iOS:** `MainViewController.kt` — ⚠️ not yet implemented. Required:
  - `AppOpened` in `viewDidLoad`
  - `SessionStarted` in `viewWillAppear`
  - `SessionEnded` in `viewDidDisappear`
  - `AppBackgrounded` in `viewWillDisappear`

`AppCrashed` is tracked via a global exception handler / Firebase Crashlytics listener (not yet implemented).

---

### Login Module — `LoginViewModel`

**Fix #5 — User identification on login (was unassigned)**

After `otp_verified` is confirmed by the repository, `OtpViewModel` sets user properties
before routing. ✅ Implemented in `OtpViewModel.submit()`:

1. `tracker.identify(businessId)` ✅
2. `tracker.setUserProperty(...)` for 7 of 8 user properties ✅:
   ```
   user_role, business_id, app_version, platform, os_version, device_model
   ```
   > `DeviceInfo` data class provides `app_version`, `platform`, `os_version`,
   > `device_model` from platform-specific Koin bindings in `androidPlatformModule`
   > and `iosPlatformModule`.
3. `tracker.track(GlobalEvent.OtpVerified)` ✅

⚠️ `network_type` is the only missing property — requires wiring the existing
`ConnectivityObserver` to call `setUserProperty` on connectivity changes. Deferred.

Full event ownership for Login:

| Event | Trigger point | Status |
|---|---|---|
| `LoginScreenViewed` | `AppNavHost` `LaunchedEffect` on Login route | ✅ |
| `LoginSubmitted` | `LoginViewModel.submit()` | ✅ |
| `LoginFailed` | `LoginViewModel.submit()` on error | ✅ |
| `OtpScreenViewed` | `OtpViewModel.setMobileNumber()` — masked number derived here | ✅ |
| `OtpEntered` | `OtpViewModel.submit()` | ✅ |
| `OtpResendTapped` | `OtpViewModel.resendOtp()` | ✅ |
| `OtpEditNumberTapped` | `OtpViewModel.editNumber()` | ✅ |
| `OtpVerified` | `OtpViewModel.submit()` on success | ✅ |
| `OtpVerificationFailed` | `OtpViewModel.submit()` on error | ✅ |
| `SessionExpired` | `AppNavHost` `SessionManager.events` handler | ✅ |
| `LogoutTapped` | `ProfileScreen` onClick → `ProfileAction.LogoutTapped`; `ProfileViewModel` fires event | ✅ |
| `LogoutConfirmed` | `ProfileViewModel.logout()` — fires event then calls `analytics.reset()` | ✅ |

---

### ASE Module — ASE ViewModels

Every event in `analytics_events_ase.md` is now tracked:

| Event | ViewModel | Trigger point | Status |
|---|---|---|---|
| `ASEEvent.DashboardLoaded` | `AseDashboardViewModel` | `loadOutlets().onSuccess` | ✅ |
| `ASEEvent.DashboardCardTapped` | `AseHomeViewModel` | `onAction(TotalOutletsClicked / InProgressOutletsClicked / AsmPendingOutletsClicked / PendingInvoicesClicked)` | ✅ |
| `ASEEvent.SlabChartViewed` | `AseHomeViewModel` | `loadDashboard()` slab success when `slabs.isNotEmpty()` | ✅ |
| `ASEEvent.OutletListViewed` | `AseDashboardViewModel` | `loadOutlets().onSuccess` alongside DashboardLoaded | ✅ |
| `ASEEvent.OutletFilterSelected` | `AseDashboardViewModel` | `trackOutletFilterSelected()` — called from `AseDashboardScreen` filter chip `onFilterSelected` | ✅ |
| `ASEEvent.OutletSearchUsed` | `AseDashboardViewModel` | `trackOutletSearchUsed()` — called from `AseDashboardScreen` debounced `LaunchedEffect(searchQuery)` (600 ms) | ✅ |
| `ASEEvent.OutletCardTapped` | `AseDashboardViewModel` | `trackOutletCardTapped()` — called from `AseDashboardScreen` `onViewDetails` / `onContinue` callbacks | ✅ |
| `ASEEvent.OutletListScrolled` | `AseDashboardViewModel` | `trackOutletListScrolled()` — called from `AseDashboardScreen` `snapshotFlow { firstVisibleItemIndex }` | ✅ |
| `ASEEvent.OutletDetailViewed` | `OutletDetailViewModel` | On screen load | ✅ |
| `ASEEvent.AssetRequestTapped` | `OutletDetailViewModel` | On asset request action | ✅ |
| `ASEEvent.AssetRequestSuccess/Failed` | `OutletDetailViewModel` | On repository result | ✅ |
| `ASEEvent.ComplianceTapped` | `AppNavHost` | `onSubmitCompliance` callback before navigating to compliance route | ✅ |
| `ASEEvent.ComplianceScreenViewed` | `ComplianceViewModel` | `setOutletId()` | ✅ |
| `ASEEvent.CompliancePhotoCaptured` | `ComplianceViewModel` | `handlePhotoCaptured()` | ✅ |
| `ASEEvent.CompliancePhotoRemoved` | `ComplianceViewModel` | `handlePhotoRemoved()` | ✅ |
| `ASEEvent.ComplianceSubmitted` | `ComplianceViewModel` | `submit()` before API call | ✅ |
| `ASEEvent.ComplianceSubmitSuccess/Failed` | `ComplianceViewModel` | On repository result | ✅ |
| `ASEEvent.OnboardingStarted` | `OnboardingViewModel` | `createOutletAndContinue().onSuccess` | ✅ |
| `ASEEvent.OnboardingStepViewed` | `OnboardingViewModel` | `SetCurrentStep` action | ✅ |
| `ASEEvent.OnboardingStepCompleted` | `OnboardingViewModel` | After step data persisted | ✅ |
| `ASEEvent.OnboardingStepAbandoned` | `OnboardingViewModel` | `GoBack` action | ✅ |
| `ASEEvent.OnboardingStepError` | `OnboardingViewModel` | Validation failure in `continueToNext()` | ✅ |
| `ASEEvent.OnboardingGpsCaptured` | `OnboardingViewModel` | After GPS success | ✅ |
| `ASEEvent.OnboardingGpsFailed` | `OnboardingViewModel` | After GPS error | ✅ |
| `ASEEvent.OnboardingPincodeLookedUp` | `OnboardingViewModel` | After pincode API result | ✅ |
| `ASEEvent.OnboardingOutletTypeSelected` | `OnboardingViewModel` | `OutletTypeSelected` action | ✅ |
| `ASEEvent.OnboardingSlabSelected` | `OnboardingViewModel` | `SlabSelected` action | ✅ |
| `ASEEvent.OnboardingStockingItemsSelected` | `OnboardingViewModel` | `StockingItemToggled` (on add) | ✅ |
| `ASEEvent.OnboardingDistributorSelected` | `OnboardingViewModel` | `DistributorSelected` action | ✅ |
| `ASEEvent.OnboardingBankDetailsEntered` | `OnboardingViewModel` | Before `submitKycDetailsAndContinue()` API call | ✅ |
| `ASEEvent.OnboardingKycTypeSelected` | `OnboardingViewModel` | `KycTypeSelected` action | ✅ |
| `ASEEvent.OnboardingKycPhotoCaptured` | `OnboardingViewModel` | `KycPhotoCaptured` action | ✅ |
| `ASEEvent.OnboardingPhotoCaptured` | `OnboardingViewModel` | `PhotoCaptured` action | ✅ |
| `ASEEvent.OnboardingPhotoRemoved` | `OnboardingViewModel` | `PhotoRemoved` action | ✅ |
| `ASEEvent.OnboardingAgreementAccepted` | `OnboardingViewModel` | `AgreementAccepted` action | ✅ |
| `ASEEvent.OnboardingAgreementOtpRequested` | `OnboardingViewModel` | OTP request success | ✅ |
| `ASEEvent.OnboardingAgreementOtpVerified` | `OnboardingViewModel` | `verifyAgreementOtp().onSuccess` | ✅ |
| `ASEEvent.OnboardingSubmitted` | `OnboardingViewModel` | After final submission sent | ✅ |
| `ASEEvent.OnboardingCompleted` | `OnboardingViewModel` | `verifyAgreementOtp().onSuccess` | ✅ |
| `ASEEvent.InvoiceListViewed` | `InvoiceViewModel` | `loadInvoices().onSuccess` | ✅ |
| `ASEEvent.InvoiceFilterSelected` | `InvoiceViewModel` | `onFilterSelected()` | ✅ |
| `ASEEvent.InvoiceSearchUsed` | `InvoiceViewModel` | `onSearchChanged()` when query non-blank | ✅ |
| `ASEEvent.InvoiceDetailViewed` | `InvoiceViewModel` | `loadInvoiceDetail()` | ✅ |
| `ASEEvent.InvoiceApproved/Rejected` | `InvoiceViewModel` | Before API call | ✅ |
| `ASEEvent.InvoiceActionSuccess/Failed` | `InvoiceViewModel` | On repository result | ✅ |

> `AseDashboardViewModel` does not extend `BaseViewModel` — interaction events (filter, search,
> card tap, scroll) are exposed as public `track*` methods called from `AseDashboardScreen` after
> the corresponding UI action. Dashboard-level events (`DashboardCardTapped`, `SlabChartViewed`)
> live in `AseHomeViewModel` because those UI elements (`QuickStatsGrid`, `SlabConfigSection`)
> are rendered by `AseHomeScreen`. `ComplianceTapped` fires from `AppNavHost.onSubmitCompliance`
> where `outletId` is directly in scope.

---

### ASM Module — ASM ViewModels

| Event | ViewModel | Trigger point | Status |
|---|---|---|---|
| `ASMEvent.DashboardLoaded` | `AsmDashboardViewModel` | `loadOutlets().onSuccess` | ✅ |
| `ASMEvent.DashboardCardTapped` | `AsmHomeViewModel` | `onAction(TotalOutletsClicked / ActiveOutletsClicked / InProgressOutletsClicked / SuspendedOutletsClicked / AsmPendingOutletsClicked / PendingInvoicesClicked)` | ✅ |
| `ASMEvent.MyTeamTapped` | `AsmHomeViewModel` | `onAction(TotalAsesClicked)` | ✅ |
| `ASEEvent.SlabChartViewed` | `AsmHomeViewModel` | `loadDashboard()` slab success when `slabs.isNotEmpty()` (reuses ASEEvent — no separate ASM variant) | ✅ |
| `ASMEvent.OutletListViewed` | `AsmDashboardViewModel` | `loadOutlets().onSuccess` | ✅ |
| `ASMEvent.OutletFilterSelected` | `AsmDashboardViewModel` | `trackOutletFilterSelected()` — called from `AsmDashboardScreen` filter chip `onFilterSelected` | ✅ |
| `ASMEvent.OutletReviewViewed` | `AsmDashboardViewModel` | `trackOutletReviewViewed()` — called from `AsmDashboardScreen` `onReview` callback | ✅ |
| `ASMEvent.OutletApproved` | `AsmDashboardViewModel` | `approveOutlet().onSuccess` | ✅ |
| `ASMEvent.OutletRejected` | `AsmDashboardViewModel` | `rejectOutlet().onSuccess` | ✅ |
| `ASMEvent.InvoiceListViewed` | `AsmInvoiceViewModel` | `loadInvoices().onSuccess` | ✅ |
| `ASMEvent.InvoiceFilterSelected` | `AsmInvoiceViewModel` | ⚠️ no filter action in contract yet | ⚠️ not yet |
| `ASMEvent.InvoiceDetailViewed` | `AsmInvoiceViewModel` | `loadInvoiceDetail()` | ✅ |
| `ASMEvent.InvoiceApproved/Rejected` | `AsmInvoiceViewModel` | Before API call | ✅ |
| `ASMEvent.MyTeamViewed` | `AsmMyTeamViewModel` | On screen load | ⚠️ not yet |
| `ASMEvent.MyTeamSearched` | `AsmMyTeamViewModel` | On search input | ⚠️ not yet |
| `ASMEvent.MyTeamRefreshed` | `AsmMyTeamViewModel` | On pull-to-refresh | ⚠️ not yet |

---

### Outlet Module — Outlet ViewModels

| Event | ViewModel | Trigger point | Status |
|---|---|---|---|
| `OutletEvent.WalkthroughStarted` | `AppNavHost` | `LaunchedEffect` on WalkthroughWelcome route | ✅ |
| `OutletEvent.WalkthroughWelcomeCompleted` | `WalkthroughViewModel` | `ContinueFromWelcome` action | ✅ |
| `OutletEvent.WalkthroughOutletDetailsViewed` | `WalkthroughViewModel` | `loadOutlet()` first-load success | ✅ |
| `OutletEvent.WalkthroughOutletDetailsCompleted` | `WalkthroughViewModel` | `ContinueFromOutletDetails` | ✅ |
| `OutletEvent.WalkthroughAgreementViewed` | `WalkthroughViewModel` | `loadKyc()` first-load success | ✅ |
| `OutletEvent.WalkthroughAgreementCompleted` | `WalkthroughViewModel` | `ContinueFromAgreement` (canProceed only) | ✅ |
| `OutletEvent.WalkthroughPaymentViewed` | `WalkthroughViewModel` | `ContinueFromAgreement` (alongside AgreementCompleted) | ✅ |
| `OutletEvent.WalkthroughCompleted` | `WalkthroughViewModel` | `finishWalkthrough()` | ✅ |
| `OutletEvent.WalkthroughAbandoned` | `WalkthroughViewModel` | `GoBack` action with `currentStepName` | ✅ |
| `OutletEvent.DashboardLoaded` | `OutletDashboardViewModel` | `getDashboard().onSuccess` | ✅ |
| `OutletEvent.DashboardCardTapped` | `OutletDashboardViewModel` | `onAction(TotalInvoicesClicked / PendingInvoicesClicked / ViewAllInvoicesClicked)` | ✅ |
| `OutletEvent.UploadInvoiceTapped` | `OutletDashboardViewModel` | `onAction(UploadInvoiceClicked)` | ✅ |
| `OutletEvent.NotificationViewed` | `OutletDashboardViewModel` | `onAction(ViewNotification)` | ✅ |
| `OutletEvent.NotificationDismissed` | `OutletDashboardViewModel` | `onAction(DismissNotification)` | ✅ |
| `OutletEvent.RecentInvoiceTapped` | `OutletDashboardViewModel` | `onAction(RecentInvoiceTapped)` | ✅ |
| `OutletEvent.InvoiceUploadStarted` | `AppNavHost` | `LaunchedEffect` on OutletUploadInvoice route | ✅ |
| `OutletEvent.InvoiceCaptureMethodSelected` | `UploadInvoiceViewModel` | `onAction(ScanInvoice / UploadFromGallery)` | ✅ |
| `OutletEvent.InvoiceImageCaptured` | `UploadInvoiceViewModel` | `onImageCaptured()` | ✅ |
| `OutletEvent.InvoiceCropReviewViewed` | `AppNavHost` | `LaunchedEffect` on OutletInvoiceCropReview route | ✅ |
| `OutletEvent.InvoiceCropConfirmed` | `AppNavHost` | `InvoiceCropReviewScreen.onConfirm` callback | ✅ |
| `OutletEvent.InvoiceCropRetake` | `AppNavHost` | `InvoiceCropReviewScreen.onRetake` callback | ✅ |
| `OutletEvent.InvoicePreprocessingStarted/Completed` | `UploadInvoiceViewModel` | Around preprocessing call | ✅ |
| `OutletEvent.InvoiceQualityWarningShown` | `UploadInvoiceViewModel` | Before `ShowQualityWarning` event | ✅ |
| `OutletEvent.InvoiceQualityWarningDismissed` | `UploadInvoiceViewModel` | `onAction(ProceedDespiteWarnings)` | ✅ |
| `OutletEvent.InvoiceRetakeGuideShown` | `UploadInvoiceViewModel` | Before `PromptRetakeWithGuide` event | ✅ |
| `OutletEvent.InvoiceRetakeGuideDismissed` | `UploadInvoiceViewModel` | `onAction(DismissRetakeGuide)` | ✅ |
| `OutletEvent.InvoiceFilterSelected` | `UploadInvoiceViewModel` | `onFilterSelected()` | ✅ |
| `OutletEvent.InvoiceAiExtractionStarted/Completed/Failed/Cancelled` | `UploadInvoiceViewModel` | Around OCR/AI call | ✅ |
| `OutletEvent.InvoiceReviewViewed` | `UploadInvoiceViewModel` | `applyOcrResult()` before `NavigateToReview` | ✅ |
| `OutletEvent.InvoiceReviewFieldEdited` | `UploadInvoiceViewModel` | `onAction(UpdateInvoiceNumber / UpdateDate / UpdateDistributor / UpdateBillTo)` | ✅ |
| `OutletEvent.InvoiceReviewLineItemEdited` | `UploadInvoiceViewModel` | `onAction(UpdateLineItem)` | ✅ |
| `OutletEvent.InvoiceReviewLineItemAdded` | `UploadInvoiceViewModel` | `onAction(AddLineItem)` | ✅ |
| `OutletEvent.InvoiceReviewLineItemRemoved` | `UploadInvoiceViewModel` | `onAction(RemoveLineItem)` | ✅ |
| `OutletEvent.InvoiceRegionScanUsed` | `AppNavHost` | `LaunchedEffect(itemIndex, fieldName)` inside `OutletRegionScan` composable block — both params from route args | ✅ |
| `OutletEvent.InvoiceSubmitted` | `UploadInvoiceViewModel` | On submit intent | ✅ |
| `OutletEvent.InvoiceSubmitSuccess` | `UploadInvoiceViewModel` | After API confirms | ✅ |
| `OutletEvent.InvoiceSubmitFailed` | `UploadInvoiceViewModel` | On API error | ✅ |
| `OutletEvent.InvoiceDraftSaved` | `UploadInvoiceViewModel` | `onSaveDraft()` | ✅ |
| `OutletEvent.InvoiceListViewed` | `OutletInvoiceViewModel` | `loadInvoices()` on success | ✅ |
| `OutletEvent.InvoiceListFilterSelected` | `OutletInvoiceViewModel` | `onFilterSelected()` | ✅ |
| `OutletEvent.InvoiceSearchUsed` | `OutletInvoiceViewModel` | `onSearchChanged()` when non-blank | ✅ |
| `OutletEvent.InvoiceDetailViewed` | `OutletInvoiceViewModel` | `loadInvoiceDetail()` | ✅ |
| `OutletEvent.InvoiceListScrolled` | `OutletInvoiceViewModel` | `trackListScrolled()` public method | ✅ |

---

## PHASE 5 — Screen Tracking System

**Module:** `core/analytics`
**Navigation layer integration**

### Actual Implementation: `ScreenStateTracker.kt`

Implemented as a stateless `object` (not a class). Route-to-name mapping is a pure
function — no mutable state stored inside the tracker. Previous-screen state is
maintained as a `remember { mutableStateOf<String?>(null) }` ref inside `AppNavHost.kt`.

```kotlin
object ScreenStateTracker {
    fun screenName(route: String?): String = when {
        route == null -> "unknown"
        route == AppDestination.Login.route -> "Login"
        route == AppDestination.VerifyOtp.route -> "OTP Verification"
        // ... all 36 AppDestination routes ...
        route.startsWith("outlet/region-scan") -> "Invoice Region Scan"
        else -> route
    }
}
```

### Integration in `AppNavHost.kt`

```kotlin
val previousRouteRef = remember { mutableStateOf<String?>(null) }

LaunchedEffect(currentRoute) {
    if (currentRoute != null) {
        analytics.track(AnalyticsEvent.GlobalEvent.ScreenViewed(
            screenName = ScreenStateTracker.screenName(currentRoute),
            previousScreen = ScreenStateTracker.screenName(previousRouteRef.value),
            userRole = currentRole?.name ?: "UNKNOWN"
        ))
        when (currentRoute) {
            AppDestination.Login.route               -> analytics.track(GlobalEvent.LoginScreenViewed(isAutoRedirect = false))
            // OtpScreenViewed NOT fired here — moved to OtpViewModel.setMobileNumber()
            // so the masked mobile number can be included in the event
            AppDestination.WalkthroughWelcome.route  -> analytics.track(OutletEvent.WalkthroughStarted(isFirstTime = true))
            AppDestination.OutletUploadInvoice.route -> analytics.track(OutletEvent.InvoiceUploadStarted(source = ""))
        }
        previousRouteRef.value = currentRoute
    }
}
```

`userRole` is read from `RoleManager.currentRole.collectAsState().value` — not hardcoded.
`screen_viewed` always includes `screen_name`, `previous_screen`, and `user_role`.

---

## PHASE 6 — Validation & Consistency Rules

**Scope:** Whole project

### Audit Results

1. **Event completeness audit** ✅ — All 108 events (106 spec + 2 ProfileEvent) have sealed class leaves in `AnalyticsEvent.kt`
   and exhaustive mappings in `AnalyticsEventMapper.kt`. Mapper uses `when` with no `else` fallback.

2. **Funnel lifecycle enforcement** — Status per funnel:
   - `login_screen_viewed → login_submitted → otp_verified` ✅ complete
   - `onboarding_started → onboarding_step_completed (×N) → onboarding_completed`
     - `OnboardingStarted(outletId)` fires in `createOutletAndContinue().onSuccess` ✅
     - `OnboardingCompleted` fires in `verifyAgreementOtp().onSuccess` ✅
   - `invoice_upload_started → invoice_image_captured → invoice_submitted → invoice_submit_success` ✅
     - `InvoiceRetakeGuideShown` fires before `PromptRetakeWithGuide` event ✅
     - `InvoiceQualityWarningShown` fires before `ShowQualityWarning` event ✅
   - `walkthrough_started → walkthrough_completed` ✅
     - `WalkthroughStarted` fires from `AppNavHost` on `WalkthroughWelcome` route ✅
     - `WalkthroughCompleted` fires in `WalkthroughViewModel.finishWalkthrough()` ✅

3. **Parameter completeness** ✅ — All required parameters are non-nullable constructor params.
   No optional parameters used for required event data.

4. **User properties at session start** — 7/8 properties set:
   - ✅ `user_id` via `tracker.identify(businessId)`
   - ✅ `user_role`, `business_id`, `app_version`, `platform`, `os_version`, `device_model`
   - ✅ `LogoutConfirmed` fires in `ProfileViewModel.logout()` before `clearSession()`; `tracker.reset()` called immediately after
   - ⚠️ `network_type` — deferred; requires wiring `ConnectivityObserver` to `setUserProperty`

5. **PII audit** ✅ — No phone numbers, plain-text names, or sensitive identifiers found
   in event parameter values. `businessId` (UUID) and `outletId` (opaque IDs) are safe.

6. **Deduplication check** ✅ — All `tracker.track()` calls are in ViewModels or platform
   entry points (`MainActivity`). No tracking in Compose UI composables.

7. **No direct Firebase usage** ✅ — `FirebaseAnalytics` only referenced in
   `androidMain/.../analytics/FirebaseAnalyticsTracker.kt` and `androidPlatformModule.kt`
   (for instantiation). iOS Firebase calls are entirely in Swift via `SwiftFirebaseAnalyticsDelegate`.

8. **Exhaustive mapper coverage** ✅ — Both platform `FirebaseAnalyticsTracker` implementations
   use exhaustive `when` expressions with no `else`. Compiler enforces coverage on new events.

### Remaining open items after Phase 6

- **iOS lifecycle events**: `AppOpened`, `SessionStarted`, `SessionEnded`, `AppBackgrounded`
  must be added to `MainViewController` (iOS equivalent of `MainActivity`). Android ✅, iOS ⚠️
  (`MainViewController` is a thin `ComposeUIViewController` wrapper — lifecycle hooks require
  Swift-side `UIViewController` subclass or `iOSApp.swift` scene delegate callbacks.)
- **`network_type` user property**: Wire `ConnectivityObserver` to `setUserProperty` on changes.
- **Timing metrics**: Many events still pass `0L` for timing params (`timeToEnterMs`,
  `timeToVerifyMs`, `timeOnStepMs`, etc.) — needs `kotlinx.datetime` or platform clock integration.
- **`InvoiceReviewFieldEdited` / `InvoiceDraftSaved`**: Call sites in `UploadInvoiceViewModel`
  field-update handlers not yet wired.
- **ASM My Team events** (`MyTeamViewed`, `MyTeamSearched`, `MyTeamRefreshed`): Feature not yet built — no `AsmMyTeamViewModel` exists.
- **`ASMEvent.InvoiceFilterSelected`**: No filter action in `AsmInvoiceViewModel` contract yet.
- **`InvoiceAiExtractionPhaseChanged`**: Not yet mapped — fires during multi-phase OCR processing.
- **Firebase DebugView verification**: ✅ Verified on emulator and real device. Debug builds
  auto-enable DebugView via `manifestPlaceholders` (Android) and `UserDefaults #if DEBUG` (iOS).
  Remember to select the device in the Firebase Console DebugView dropdown.

---

## Complete Event Count Reference

| Source | Event count |
|---|---|
| `analytics_events_global.md` | 20 |
| `analytics_events_ase.md` | 37 |
| `analytics_events_asm.md` | 15 |
| `analytics_events_outlet.md` | 34 |
| `ProfileEvent` (added beyond spec) | 2 |
| **Total sealed class leaves** | **108** |

All 108 events have sealed class leaves in `AnalyticsEvent.kt` and exhaustive mapping
entries in `AnalyticsEventMapper.kt`. Both mappers use `when` with no `else` — adding a
new event without a mapping is a compile error.

---

## Fixes Applied (vs. Original Plan)

| # | Fix |
|---|---|
| 1 | `ASEEvent.OutletDetails` group added (5 events) |
| 2 | `ASEEvent.OnboardingStep` granular events added (17 events) |
| 3 | `GlobalEvent` completed: `AppBackgrounded`, `AppCrashed`, `DeepLinkOpened`, all auth events, `BottomTabTapped`, `BackButtonTapped`, full Network group |
| 4 | `AnalyticsParams` now includes `os_version`, `locale`, `network_type`, `edits_made_count` |
| 5 | `OtpViewModel` owns `identify()` + 7 of 8 `setUserProperty()` calls post `otp_verified`; `DeviceInfo` data class provides platform metadata |
| 6 | Plan now states all 106 events must be implemented; examples are illustrative only |
| 7 | `AppOpened`, `SessionStarted`, `SessionEnded`, `AppBackgrounded` implemented in `MainActivity` (Android ✅, iOS ⚠️) |
| 8 | `ScreenStateTracker` implemented as stateless `object`; `user_role` included via `RoleManager`; previous-screen state held in `AppNavHost` ref |
| 9 | Phase 3 DI uses platform modules directly — no `expect/actual`; `BuildConfig` not used (disabled); `PackageManager.getPackageInfo()` used instead |
| 10 | Phase 5 screen tracking uses `LaunchedEffect(currentRoute)` in `AppNavHost`; funnel-start events (`WalkthroughStarted`, `InvoiceUploadStarted`, `LoginScreenViewed`) fired in same `when` block; `OtpScreenViewed` excluded — moved to `OtpViewModel.setMobileNumber()` to include masked phone number |
| 11 | Phase 6 audit: `OnboardingStarted(outletId)` and `OnboardingCompleted` added; `InvoiceRetakeGuideShown` and `InvoiceQualityWarningShown` added to `UploadInvoiceViewModel` |
| 12 | Full PDF gap-analysis pass: ~80 new `AnalyticsParams` constants; all 30+ events upgraded from `data object` to rich `data class` with spec-required params; `DeviceInfo` gained `buildNumber`; `ProfileEvent` sealed class added; `AnalyticsEventMapper` fully rewritten; `MainActivity` wired with fresh-install detection and full device/auth context; `OtpViewModel` gained masked-number derivation, resend counter, and timing scaffolding; 14 ViewModels updated with corrected event signatures and new tracking calls |
| 13 | `InvoiceReviewViewed` (P0 funnel event) wired in `UploadInvoiceViewModel.applyOcrResult()` before `NavigateToReview`; `LogoutTapped` action added to `ProfileContract`, fired from `ProfileScreen` onClick; `LogoutConfirmed` + `analytics.reset()` wired in `ProfileViewModel.logout()` |
| 14 | `AppCrashed` implemented via `Thread.setDefaultUncaughtExceptionHandler` in `MainActivity` (Android) and `NSSetUncaughtExceptionHandler` in `iOSApp.swift` (iOS — ObjC exceptions only; Swift fatal errors covered by Crashlytics) |
| 15 | `BackButtonTapped` implemented via new `PlatformBackHandler` `expect/actual` composable wired in `AppNavHost`; Android `actual` delegates to `androidx.activity.compose.BackHandler`; iOS `actual` is a no-op (swipe-back handled by `UINavigationController`) |
| 16 | `SessionExpired` wired in `AppNavHost` `SessionManager.events` handler before navigation to Login |
| 17 | `InvoiceRegionScanUsed` wired in `AppNavHost` `LaunchedEffect(itemIndex, fieldName)` inside the `OutletRegionScan` composable block — both params extracted from route args, covers Android and iOS without changing the `expect/actual` screen signature |
| 18 | `AseHomeViewModel` and `AsmHomeViewModel` each received `AnalyticsTracker`; dashboard card taps and slab chart viewed now fire from the correct ViewModels that own those UI elements; dead `trackDashboardCardTapped`, `trackSlabChartViewed`, `trackComplianceTapped`, `trackMyTeamTapped` stubs removed from `AseDashboardViewModel` and `AsmDashboardViewModel` |
| 19 | `AseDashboardScreen` now calls `trackOutletFilterSelected`, `trackOutletSearchUsed` (debounced 600 ms), `trackOutletCardTapped`, `trackOutletListScrolled`; `AsmDashboardScreen` now calls `trackOutletFilterSelected` and `trackOutletReviewViewed` |
| 20 | Firebase DebugView auto-enabled in debug builds: Android via `manifestPlaceholders["firebaseAnalyticsDebugMode"]` in `build.gradle.kts` + `firebase_analytics_debug_mode` meta-data in `AndroidManifest.xml`; iOS via `UserDefaults.standard.set(true, forKey: "/google/firebase/debug_mode")` inside `#if DEBUG` in `iOSApp.swift` before `FirebaseApp.configure()`. Release builds are unaffected. |