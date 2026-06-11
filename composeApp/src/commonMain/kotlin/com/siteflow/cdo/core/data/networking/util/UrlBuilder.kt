package com.siteflow.cdo.core.data.networking.util


fun buildUrl(base: String, path: String): String =
    "${base.trimEnd('/')}/${path.trimStart('/')}"
