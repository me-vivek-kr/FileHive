package com.viv3k.filehive.ui.components

import com.viv3k.filehive.R
import java.io.File

object FileIcons {

    private val imageExt = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
    private val videoExt = setOf("mp4", "mkv", "mov", "avi", "wmv", "flv", "webm")
    private val audioExt = setOf("mp3", "wav", "ogg", "aac", "m4a", "flac")
    private val docExt   = setOf("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt")
    private val zipExt   = setOf("zip", "rar", "7z", "tar", "gz", "apk")

    fun getIcon(file: File): Int {

        if (file.isDirectory) {
            return R.drawable.folder_new     // folder icon
        }

        val name = file.name.lowercase()

        val ext = name.substringAfterLast('.', missingDelimiterValue = "")

        return when {
            imageExt.contains(ext) -> R.drawable.category_images
            videoExt.contains(ext) -> R.drawable.category_videos
            audioExt.contains(ext) -> R.drawable.category_audio
            docExt.contains(ext)   -> R.drawable.category_audio
            zipExt.contains(ext)   -> R.drawable.category_archives

            name.endsWith(".apk")  -> R.drawable.category_apk
            name.endsWith(".pdf")  -> R.drawable.category_pdf
            name.endsWith(".txt")  -> R.drawable.category_documents

            else -> R.drawable.category_documents
        }
    }
}