package com.viv3k.filehive.ui.components

import com.viv3k.filehive.R
import java.io.File

object FileIcons {

    private val imageExt = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif",
        "svg", "ico",
        "dng", "raw", "nef", "cr2"
    )

    private val videoExt = setOf(
        "mp4", "mkv", "mov", "avi", "wmv", "flv", "webm", "3gp", "mpeg", "mpg", "ts", "m4v"
    )

    private val audioExt = setOf(
        "mp3", "wav", "ogg", "aac", "m4a", "flac", "amr", "mid", "opus", "aiff", "alac"
    )

    private val wordExt = setOf(
        "doc", "docx"
    )

    private val excelExt = setOf(
        "xls", "xlsx"
    )

    private val pptExt = setOf(
        "ppt", "pptx"
    )

    private val pdfExt = setOf(
        "pdf"
    )

    private val textExt = setOf(
        "txt", "md", "csv", "log", "json", "xml", "yaml", "yml", "ini"
    )

    private val fontExt = setOf(
        "ttf", "otf", "woff", "woff2", "pfb"
    )

    private val archiveExt = setOf(
        "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "cab"
    )

    private val androidAppExt = setOf(
        "apk"
    )

    private val androidPackageExt = setOf(
        "aab", "xapk", "apkm", "apks"
    )

    private val codeExt = setOf(
        "java", "kt", "cpp", "c", "h", "py", "js", "html", "css", "sh"
    )

    private val backupExt = setOf(
        "bak", "backup", "tmp", "old"
    )

    fun getIcon(file: File): Int {

        if (file.isDirectory) {
            return R.drawable.folder_new
        }

        val ext = file.extension.lowercase()

        return when {
            imageExt.contains(ext)          -> R.drawable.image_file
            videoExt.contains(ext)          -> R.drawable.video_file
            audioExt.contains(ext)          -> R.drawable.audio_file

            wordExt.contains(ext)           -> R.drawable.doc_file
            excelExt.contains(ext)          -> R.drawable.excel_file
            pptExt.contains(ext)            -> R.drawable.ppt_file
            pdfExt.contains(ext)            -> R.drawable.pdf_file
            textExt.contains(ext)           -> R.drawable.text_file

            fontExt.contains(ext)           -> R.drawable.font_file
            archiveExt.contains(ext)        -> R.drawable.archive_file

            androidAppExt.contains(ext)     -> R.drawable.apk_file
            androidPackageExt.contains(ext) -> R.drawable.category_apk

            codeExt.contains(ext)           -> R.drawable.code_file
            backupExt.contains(ext)         -> R.drawable.backup_file

            else                            -> R.drawable.blank_file
        }
    }
}