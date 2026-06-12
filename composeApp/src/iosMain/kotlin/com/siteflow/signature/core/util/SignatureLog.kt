package com.siteflow.signature.core.util

import platform.Foundation.NSLog

actual object SignatureLog {
    actual fun d(tag: String, message: String) {
        NSLog("[$tag] $message")
    }
    actual fun e(tag: String, message: String) {
        NSLog("[ERROR][$tag] $message")
    }
}
