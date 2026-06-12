package com.siteflow.signature.core.domain


import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object FileProviderUtil {

    fun getUri(
        context: Context,
        file: File
    ): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
