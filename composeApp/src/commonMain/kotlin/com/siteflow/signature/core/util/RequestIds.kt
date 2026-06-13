package com.siteflow.signature.core.util

import kotlin.random.Random

/**
 * RFC 4122 v4 UUID string, generated without any external/expect-actual deps
 * so it works identically on Android and iOS.
 */
fun randomUuid(): String {
    val bytes = ByteArray(16).also { Random.nextBytes(it) }
    bytes[6] = (bytes[6].toInt() and 0x0F or 0x40).toByte() // version 4
    bytes[8] = (bytes[8].toInt() and 0x3F or 0x80).toByte() // variant
    return buildString {
        bytes.forEachIndexed { i, byte ->
            if (i == 4 || i == 6 || i == 8 || i == 10) append('-')
            append(byte.toInt().and(0xFF).toString(16).padStart(2, '0'))
        }
    }
}

/**
 * Unique key for the `Idempotency-Key` header the backend requires on every
 * mutating request (POST/PUT/PATCH/DELETE). Stays well under the backend's
 * 80-char limit (`idem-` + 36-char UUID = 41 chars).
 */
fun idempotencyKey(): String = "idem-${randomUuid()}"
