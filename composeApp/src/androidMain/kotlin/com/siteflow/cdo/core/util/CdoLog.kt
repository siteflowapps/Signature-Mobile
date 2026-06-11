package com.siteflow.cdo.core.util

import android.util.Log

actual object CdoLog {
    actual fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
    actual fun e(tag: String, message: String) {
        Log.e(tag, message)
    }
}
