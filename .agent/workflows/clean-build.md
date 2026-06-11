---
description: Clean the project build directories and cache
---

Use this workflow whenever you encounter strange Gradle caching issues, unresolved references in Compose Multiplatform, or immediately after updating core dependencies.

1. **Clean Project**
This wipes the `build/` directories across all modules.
// turbo
```bash
./gradlew clean
```

2. **Stop Gradle Daemons (Optional)**
If Koin or Kotlin Symbol Processing (KSP) is severely stuck, you can stop the gradle daemons.
```bash
./gradlew --stop
```
