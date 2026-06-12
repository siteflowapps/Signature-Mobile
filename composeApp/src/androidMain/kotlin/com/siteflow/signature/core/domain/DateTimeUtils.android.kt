package com.siteflow.signature.core.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun nowLabel(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
