package com.viv3k.filehive.data.model

import java.io.File

data class FolderModel(
    val file: File,
    val fileCount: Int,
    val totalSize: Long,
    val lastModified: Long
)