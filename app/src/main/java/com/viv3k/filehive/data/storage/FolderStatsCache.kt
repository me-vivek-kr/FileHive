package com.viv3k.filehive.data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object FolderStatsCache {
    private data class CachedStats(
        val stats: Triple<Int, Long, Long>, // fileCount, totalSize, latestLastModified
        val observedLastModified: Long,
        val timestampMs: Long
    )

    private val cache = ConcurrentHashMap<String, CachedStats>()

    /**
     * Get stats for [root]. If cached value exists and the folder's lastModified
     * hasn't changed, return cached result. Otherwise recompute on [Dispatchers.IO].
     */
    suspend fun getStats(root: File): Triple<Int, Long, Long> {
        val path = root.absolutePath
        val currentLm = root.lastModified()

        val cached = cache[path]
        if (cached != null && cached.observedLastModified == currentLm) {
            return cached.stats
        }

        val stats = withContext(Dispatchers.IO) {
            computeFolderStats(root)
        }

        cache[path] = CachedStats(stats = stats, observedLastModified = currentLm, timestampMs = System.currentTimeMillis())
        return stats
    }

    /** Remove cache entry for `path` */
    fun invalidate(path: String) {
        cache.remove(path)
    }

    /** Clear all cached entries */
    fun clear() {
        cache.clear()
    }
}