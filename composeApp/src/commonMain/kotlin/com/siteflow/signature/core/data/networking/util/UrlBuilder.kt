package com.siteflow.signature.core.data.networking.util


fun buildUrl(base: String, path: String): String =
    "${base.trimEnd('/')}/${path.trimStart('/')}"
