package com.viv3k.filehive.data.storage

import android.os.StatFs

/** Helper: compute StorageStats for an arbitrary mount path using StatFs */
fun getStorageStatsForPath(path: String): StorageStats {
    return try {
        val stat = StatFs(path)
        val blockSize = stat.blockSizeLong
        val total = stat.blockCountLong * blockSize
        val available = stat.availableBlocksLong * blockSize
        val used = (total - available).coerceAtLeast(0L)
        val percent = if (total > 0L) (used.toFloat() / total.toFloat() * 100f) else 0f
        StorageStats(totalBytes = total, usedBytes = used, usedPercent = percent, freeBytes = available)
    } catch (e: Exception) {
        // fallback empty
        StorageStats(totalBytes = 0L, usedBytes = 0L, usedPercent = 0f, freeBytes = 0L)
    }
}