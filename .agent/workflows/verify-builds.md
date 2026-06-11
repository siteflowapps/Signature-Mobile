---
description: Verify both Android and iOS builds in this KMP project.
---

To ensure cross-platform compatibility, always run these checks:

1. **Android Build Check**
// turbo
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

2. **iOS Build Check**
// turbo
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

3. **Verify results**
Ensure both commands return `BUILD SUCCESSFUL`.
