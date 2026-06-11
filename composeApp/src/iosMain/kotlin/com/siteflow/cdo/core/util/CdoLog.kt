package com.siteflow.cdo.core.util

import platform.Foundation.NSLog

actual object CdoLog {
    actual fun d(tag: String, message: String) {
        NSLog("[$tag] $message")
    }
    actual fun e(tag: String, message: String) {
        NSLog("[ERROR][$tag] $message")
    }
}
