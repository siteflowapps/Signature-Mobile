package com.siteflow.signature.core.domain

object EmailUtils {
    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$")

    fun isValid(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }
}
