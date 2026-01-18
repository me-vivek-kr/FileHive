package com.viv3k.filehive.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FolderCacheDao{

    //Get Cache for a Specific Folder
    @Query("SELECT * FROM folder_cache WHERE path = :path")
    suspend fun getFolderCache(path: String): FolderCacheEntity?

    // Insert or Update (Replace if path already exists)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cache: FolderCacheEntity)

    //Update only the viewMode for a specific folder
    @Query("UPDATE folder_cache SET viewMode = :mode WHERE path = :path")
    suspend fun updateViewMode(path: String, mode: String)
}