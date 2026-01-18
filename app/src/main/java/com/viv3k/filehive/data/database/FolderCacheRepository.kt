package com.viv3k.filehive.data.database

import com.viv3k.filehive.data.storage.FolderStatsCache
import java.io.File

class FolderCacheRepository(private val dao: FolderCacheDao){
    private fun normalize(path: String) = path.trimEnd('/')


    suspend fun getFolderInfo(file: File): FolderCacheEntity {
        val path = normalize(file.absolutePath)

        val realLastModified = file.lastModified()

        //Try to get from DB
        val cached = dao.getFolderCache(path)

        //Check if cache exists and is Up-to-Date
        if(cached != null && cached.lastModified == realLastModified){
            return cached
        }

        //If missing or outdated, calculate fresh stats
        //(Assuming you have a helper function to calculate size)
        val (count, size, _) = FolderStatsCache.getStats(file)

        val newEntry = FolderCacheEntity(
            path = path,
            sizeBytes = size,
            fileCount = count,
            lastModified = realLastModified,
            viewMode = cached?.viewMode ?: "list" //Preserve ViewMode if Updating
        )

        dao.insertOrUpdate(newEntry)

        return newEntry
    }

    suspend fun getCache(path: String): FolderCacheEntity? {
        return dao.getFolderCache(path)
    }

    suspend fun setViewMode(path: String, isGrid: Boolean){
        val mode = if (isGrid) "grid" else "list"

        dao.updateViewMode(path, mode)
    }
}