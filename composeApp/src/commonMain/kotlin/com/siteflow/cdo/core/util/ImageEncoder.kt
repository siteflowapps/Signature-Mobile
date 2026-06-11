package com.siteflow.cdo.core.util

expect object ImageEncoder {
    fun encodeToBytes(imagePath: String, maxSize: Long = 2_097_152): ByteArray?
}
