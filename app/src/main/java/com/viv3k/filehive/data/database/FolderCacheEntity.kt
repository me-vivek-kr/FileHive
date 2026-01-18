package com.viv3k.filehive.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_cache")
data class FolderCacheEntity(
    @PrimaryKey val path: String, //Folder Path
    val sizeBytes: Long,
    val fileCount: Int,
    val lastModified: Long, //Used to Check if we need to Update the cache
    val viewMode: String = "List" //Default to list, can be "grid"
)