package com.siteflow.cdo.core.logger

actual object ReceeLogger {

    actual fun d(tag: String, message: String) {
        println("DEBUG [$tag] $message")
    }

    actual fun i(tag: String, message: String) {
        println("INFO [$tag] $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            println("ERROR [$tag] $message\n${throwable.stackTraceToString()}")
        } else {
            println("ERROR [$tag] $message")
        }
    }
}
