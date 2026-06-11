import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseAnalytics

// MARK: - Analytics Bridge

/// Implements the Kotlin AnalyticsNativeDelegate protocol, forwarding every
/// call to the Firebase Analytics iOS SDK. This class is the only place in the
/// iOS codebase that imports FirebaseAnalytics directly.
class SwiftFirebaseAnalyticsDelegate: AnalyticsNativeDelegate {

    func logEvent(name: String, parameters: [String: Any]) {
        Analytics.logEvent(name, parameters: parameters)
    }

    func setUserId(userId: String?) {
        Analytics.setUserID(userId)
    }

    func setUserProperty(name: String, value: String) {
        Analytics.setUserProperty(value, forName: name)
    }

    func resetAnalyticsData() {
        Analytics.resetAnalyticsData()
    }
}

// MARK: - Crash Handler

/// Tracks app_crashed via Firebase Analytics before the process terminates.
/// Firebase writes events to its local store synchronously, so the event
/// survives the crash and is uploaded on the next launch.
private func installCrashHandler() {
    NSSetUncaughtExceptionHandler { exception in
        Analytics.logEvent("app_crashed", parameters: nil)
    }
}

// MARK: - App Entry Point

@main
struct CDOApp: App {

    // Retained for the lifetime of the process so NSNotificationCenter observers stay alive.
    private let lifecycleTracker: AppLifecycleTracker

    init() {
        let firebaseEnv = Bundle.main.object(forInfoDictionaryKey: "FIREBASE_ENV") as? String ?? "QA"
        if firebaseEnv != "PROD" {
            UserDefaults.standard.set(true, forKey: "/google/firebase/debug_mode")
        }
        FirebaseApp.configure()

        let analyticsModule = AnalyticsModuleKt.iosAnalyticsModule(
            delegate: SwiftFirebaseAnalyticsDelegate()
        )

        KoinInitializer().start(
            platformModules: [IosPlatformModuleKt.iosPlatformModule, analyticsModule]
        )

        installCrashHandler()

        let isFreshInstall: Bool = {
            let key = "app_launched_before"
            let isFirst = !UserDefaults.standard.bool(forKey: key)
            if isFirst { UserDefaults.standard.set(true, forKey: key) }
            return isFirst
        }()

        let tracker = AppLifecycleTracker()
        tracker.trackAppOpened(isFreshInstall: isFreshInstall)
        tracker.start()
        lifecycleTracker = tracker
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}