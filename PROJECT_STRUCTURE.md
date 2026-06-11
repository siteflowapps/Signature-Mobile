# CDO-Mobile — Project Structure

## Overview

CDO-Mobile is a **Kotlin Multiplatform (KMP)** mobile application targeting Android and iOS. It shares business logic and UI (via Compose Multiplatform) across both platforms, while delegating platform-specific capabilities (OCR, secure storage, location) to native implementations.

---

## Top-Level Layout

```
CDO-Mobile/
├── composeApp/                # Shared KMP source + platform-specific code
│   └── src/
│       ├── commonMain/        # Shared Kotlin code (business logic + UI)
│       ├── androidMain/       # Android-specific implementations
│       ├── iosMain/           # iOS-specific implementations
│       └── commonTest/        # Shared unit tests
├── iosApp/                    # Xcode project (iOS app entry point)
├── gradle/
│   └── libs.versions.toml     # Centralized dependency versions
├── build.gradle.kts           # Root build configuration
├── settings.gradle.kts        # Project structure & repositories
├── gradle.properties          # Kotlin/Android build flags
└── local.properties           # Local SDK paths (not version controlled)
```

---

## Architecture

The project follows **Clean Architecture** with a **feature-based module structure**:

```
Presentation (Compose Screens + ViewModels)
        ↓
Domain   (Business Logic, Use Cases, Services)
        ↓
Data     (Networking, Storage, DTOs)
```

Dependency injection is handled by **Koin**, with each feature module registering its own DI graph.

---

## Source Structure: `commonMain`

All shared code lives under:
```
composeApp/src/commonMain/kotlin/com/siteflow/cdo/
```

### Feature Modules

#### `ase/` — Area Sales Executive (66 files)
| Sub-package | Purpose |
|---|---|
| `compliance/` | Compliance checks and rules |
| `dashboard/` | ASE dashboard screens and ViewModels |
| `invoices/` | Invoice list, detail, and management |
| `onboarding/` | Multi-step onboarding with DTOs |
| `profile/` | User profile view and editing |
| `di/` | Koin module for ASE features |

#### `outlet/` — Outlet Management (50 files)
| Sub-package | Purpose |
|---|---|
| `dashboard/` | Outlet overview and metrics |
| `invoices/` | Invoice capture, scanning, and display |
| `walkthrough/` | Guided setup/onboarding wizard |
| `di/` | Koin module for outlet features |

#### `asm/` — Account Sales Manager (10 files)
| Sub-package | Purpose |
|---|---|
| `dashboard/` | Aggregated sales dashboard |
| `invoices/` | Invoice review and management |
| `di/` | Koin module for ASM features |

#### `login/` — Authentication (18 files)
| Sub-package | Purpose |
|---|---|
| `data/` | Auth DTOs and data models |
| `domain/` | Login ViewModels and business logic |
| `presentation/` | Login and OTP screens |
| `di/` | Koin module for login |

---

### `core/` — Shared Infrastructure (71 files)

#### `auth/`
- Token storage interface (platform-specific: Android/iOS)
- Authentication interceptor for HTTP requests
- JWT utilities

#### `config/`
- Configuration API and DTOs (feature flags, remote config)

#### `networking/`
```
client/     → Ktor HTTP client setup
error/      → Error parsing and domain error mappings
request/    → Safe call wrappers
response/   → Response mapping utilities
result/     → NetworkResult type (Success/Error/Loading)
util/       → URL builders, shared network utilities
```

#### `domain/`
- Image processing, quality validation, and annotation
- OCR service interface
- Location services
- Connectivity observer
- Session manager and role management
- Invoice filter and preprocessor interfaces

#### `navigation/`
- App-level routing and screen destinations

#### `presentation/`
- Shared design system: colors, typography
- Reusable UI components:
  - Buttons, text fields, dropdowns
  - Loaders, toast notifications, filters
  - Skeleton / empty / error state components
  - Staggered animation components
  - Pagination components

#### `logger/`
- Logging wrapper (Recee Logger → Firebase Crashlytics)

#### `platform/`
- Platform-specific app settings interface

---

### `shared/` — Cross-Feature Components (5 files)
- `components/` — Approval timeline, payout estimate UI
- `data/` — Approval and payout calculation APIs

### `di/` — Root DI Graph (5 files)
| File | Purpose |
|---|---|
| `KoinInit.kt` | Koin initialization entry point |
| `AppModule.kt` | Core app-level dependencies |
| `NetworkModule.kt` | HTTP client binding |
| `CoroutineModule.kt` | Coroutine dispatcher setup |
| `ViewModelModule.kt` | ViewModel registrations |

---

## Platform-Specific Implementations

### Android (`androidMain/`)
| File | Purpose |
|---|---|
| `MainActivity.kt` | App entry point |
| `AndroidTokenStorage.kt` | Encrypted SharedPreferences token storage |
| `AndroidHttpClient.kt` | OkHttp-backed Ktor client |
| `OcrService.android.kt` | ML Kit text recognition |
| `AndroidLocationProvider.kt` | Google Play Services location |
| `FileProviderUtil.kt` | FileProvider for camera image sharing |

**Android resources:**
- Launcher icons (multiple densities)
- Inter font family
- `network_security_config.xml`, `file_paths.xml`
- Firebase `google-services.json` integration

**Permissions (AndroidManifest):** `CAMERA`, `ACCESS_FINE_LOCATION`, `INTERNET`

### iOS (`iosMain/`)
| File | Purpose |
|---|---|
| `MainViewController.kt` | iOS UIViewController entry point |
| `IosTokenStorage.kt` | Keychain token storage |
| `IosHttpClient.kt` | Darwin/URLSession Ktor client |
| `OcrService.ios.kt` | Vision framework OCR |
| `LocationService.ios.kt` | Core Location |
| `ImageProcessor.ios.kt` | Image manipulation |
| `ImagePicker.ios.kt` | Photo library / camera picker |
| `ConnectivityObserver.ios.kt` | NWPathMonitor |
| `InvoicePreprocessor.ios.kt` | Invoice image preprocessing |
| `RegionScanScreen.ios.kt` | iOS camera region scanning |
| `ImageAnnotator.kt` | Image annotation overlay |
| `DateTimeUtils.ios.kt` | iOS date/time formatting |

---

## Tests (`commonTest/`)

14 shared unit test files:

| Test | Coverage |
|---|---|
| `NetworkResultTest` | NetworkResult type handling |
| `JwtUtilsTest` | JWT parsing and validation |
| `ErrorParserTest` | API error mapping |
| `InvoiceOcrParserTest` | OCR parsing (normal + edge cases) |
| `ImageQualityValidatorTest` | Image quality thresholds |
| `OnboardingValidatorTest` | Onboarding field validation |
| `ProductMasterCatalogTest` | Product catalog logic |
| DTO serialization tests | JSON round-trip checks |

**Framework:** Kotlin Test (shared, no platform dependency)

---

## Key Technologies

| Category | Library / Tool | Version |
|---|---|---|
| Language | Kotlin Multiplatform | 2.2.21 |
| UI | Compose Multiplatform | 1.9.3 |
| Build | Gradle | 8.14.3 |
| Android plugin | AGP | 8.13.2 |
| Networking | Ktor Client | 3.3.3 |
| Serialization | Kotlinx Serialization | 1.9.0 |
| DI | Koin | 4.1.1 |
| Async | Coroutines | 1.9.0 |
| Image loading | Coil 3 | — |
| OCR (Android) | ML Kit Text Recognition + Doc Scanner | — |
| OCR (iOS) | Vision Framework (native) | — |
| Analytics | Firebase Analytics + Crashlytics | 33.12.0 |
| Location (Android) | Google Play Services Location | 21.3.0 |
| Navigation | Navigation Compose | 2.9.1 |
| Image zoom | ZoomImage Compose | 1.4.0 |

---

## User Roles

The app supports three distinct roles, each with its own module and navigation flow:

| Role | Description |
|---|---|
| **ASE** (Area Sales Executive) | Field sales — captures invoices, manages outlets, tracks compliance |
| **Outlet** | Outlet-side user — scans invoices, completes walkthroughs |
| **ASM** (Account Sales Manager) | Manager — reviews sales data, approves invoices |

Role is determined post-login and routes the user to the appropriate feature graph.

---

## Developer Utilities

| File | Purpose |
|---|---|
| `update_models.py` | Batch-replace model field names across the codebase |
| `update_screen.py` | Standardize colors and component layouts across screens |
| `.agent/workflows/` | Automation scripts: build verification, test runs, git push, clean builds |