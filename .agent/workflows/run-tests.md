---
description: Run the test suite for the project
---

Follow these steps to run the test suite and ensure no regressions in your Kotlin Multiplatform business logic.

1. **Run Unit Tests (Android)**
Execute all unit tests for the Android target.
// turbo
```bash
./gradlew :composeApp:testDebugUnitTest
```

2. **Run KMP Common Tests (Optional)**
Execute tests shared across all platforms in `commonTest`.
```bash
./gradlew :composeApp:allTests
```
