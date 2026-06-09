package com.viv3k.filehive.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_folders")
data class LockedFolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalPath: String,
    val vaultPath: String,
    val folderName: String,
    val lockedTime: Long,
    val itemsCount: Int = 0 // Cached item count for UI
)
