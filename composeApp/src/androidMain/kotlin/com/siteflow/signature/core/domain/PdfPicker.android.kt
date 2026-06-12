package com.siteflow.signature.core.domain

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Android actual for [PdfPicker]. Uses the Storage Access Framework via
 * `ActivityResultContracts.OpenDocument` filtered to `application/pdf`.
 *
 * The launcher must be registered before the Activity reaches STARTED, so
 * [bind] is invoked from `MainActivity.onCreate` (mirroring [ImagePicker.bind]).
 */
actual class PdfPicker {

    private lateinit var activity: ComponentActivity
    private lateinit var launcher: ActivityResultLauncher<Array<String>>
    private var pending: ((PdfPickResult) -> Unit)? = null

    fun bind(activity: ComponentActivity) {
        this.activity = activity
        launcher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            val cb = pending ?: return@registerForActivityResult
            pending = null
            if (uri == null) {
                cb(PdfPickResult.Cancelled)
                return@registerForActivityResult
            }
            try {
                val bytes = readBytes(uri)
                val filename = queryDisplayName(uri) ?: "invoice.pdf"
                cb(PdfPickResult.Success(bytes = bytes, filename = filename))
            } catch (t: Throwable) {
                cb(PdfPickResult.Error(t.message ?: "Could not read PDF"))
            }
        }
    }

    actual fun pickPdf(onPicked: (PdfPickResult) -> Unit) {
        pending = onPicked
        launcher.launch(arrayOf("application/pdf"))
    }

    private fun readBytes(uri: Uri): ByteArray =
        activity.contentResolver.openInputStream(uri).use { stream ->
            requireNotNull(stream) { "Could not open PDF stream" }
            stream.readBytes()
        }

    private fun queryDisplayName(uri: Uri): String? {
        val cursor = activity.contentResolver.query(uri, null, null, null, null) ?: return null
        return cursor.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && it.moveToFirst()) it.getString(idx) else null
        }
    }
}
