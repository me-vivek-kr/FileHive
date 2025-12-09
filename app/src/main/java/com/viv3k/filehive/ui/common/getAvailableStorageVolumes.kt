package com.viv3k.filehive.ui.common

import android.content.Context
import android.os.Environment
import com.viv3k.filehive.ui.Model.DeviceStorage
import com.viv3k.filehive.ui.utils.StorageStats
import java.io.File

/** Helper: discover available storage roots (internal + removable volumes) */
fun getAvailableStorageVolumes(context: Context): List<DeviceStorage> {
    val roots = mutableListOf<DeviceStorage>()
    val dirs = context.getExternalFilesDirs(null)
    for ((index, dir) in dirs.withIndex()) {
        if (dir == null) continue
        // strip app-specific suffix ("/Android/...") to get actual mount root
        val rootPath = dir.absolutePath.substringBefore("/Android/")
        if (roots.any { it.path == rootPath }) continue

        val isPrimary = index == 0

        // check storage stats and skip removable volumes with no usable space
        val stats = try {
            getStorageStatsForPath(rootPath)
        } catch (_: Throwable) {
            StorageStats(totalBytes = 0L, usedBytes = 0L, usedPercent = 0f, freeBytes = 0L)
        }

        // If not primary and the mount reports zero total space, skip it
        if (!isPrimary && stats.totalBytes <= 0L) continue

        val removable = try {
            Environment.isExternalStorageRemovable(File(rootPath))
        } catch (_: Throwable) {
            false
        }

        val label = when {
            isPrimary -> "Internal Storage"
            removable && rootPath.contains("usb", ignoreCase = true) -> "USB OTG"
            removable -> "Memory Card"
            else -> "External Storage"
        }

        roots.add(DeviceStorage(id = rootPath, label = label, path = rootPath))
    }
    return roots
}