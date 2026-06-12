package com.siteflow.signature.core.domain

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun nowLabel(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "dd/MM/yyyy HH:mm"
    return formatter.stringFromDate(NSDate())
}
