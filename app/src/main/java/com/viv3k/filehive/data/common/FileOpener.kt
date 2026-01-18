package com.viv3k.filehive.data.common


import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object FileOpener{

    fun openFile(context: Context, file: File){
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }

        try{
            // Get Secure Content URI
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            // Determine Mime Type
            val extension = file.extension.lowercase()
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

            // Fallbacks for common types if MimeTypeMap fails
            if (mimeType == null) {
                mimeType = when (extension) {
                    "apk" -> "application/vnd.android.package-archive"
                    "pdf" -> "application/pdf"
                    "doc", "docx" -> "application/msword"
                    "xls", "xlsx" -> "application/vnd.ms-excel"
                    "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                    "txt" -> "text/plain"
                    else -> "*/*"
                }
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception){
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}