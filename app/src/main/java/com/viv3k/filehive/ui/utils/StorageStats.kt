package com.viv3k.filehive.ui.utils

import android.os.Environment
import android.os.StatFs

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercent: Double
)

fun readStorageStats(): StorageStats{
    val path = Environment.getExternalStorageDirectory().absolutePath
    val stat = StatFs(path)

    val totalBytes = stat.blockSizeLong * stat.blockCountLong
    val freeBytes = stat.blockSizeLong * stat.availableBlocksLong
    val usedBytes = totalBytes - freeBytes

    val usedPercent =
        if (totalBytes > 0) usedBytes.toDouble() * 100f / totalBytes.toDouble()
        else 0.0

    return StorageStats(
        totalBytes = totalBytes,
        usedBytes = usedBytes,
        freeBytes = freeBytes,
        usedPercent = usedPercent
    )
}

fun Long.toGbString(decimals: Int = 1): String {
    val gb = this.toDouble() / (1024.0 * 1024.0 * 1024.0)
    return String.format("%.${decimals}f GB", gb)
}