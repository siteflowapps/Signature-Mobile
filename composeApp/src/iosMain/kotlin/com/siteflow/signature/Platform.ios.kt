package com.siteflow.signature

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val appVersion: String = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
    override val clientType: String = "iOS"
    override val osVersion: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val deviceModel: String = UIDevice.currentDevice.model
}

actual fun getPlatform(): Platform = IOSPlatform()

actual val buildEnvironment: String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("FIREBASE_ENV") as? String ?: "QA"