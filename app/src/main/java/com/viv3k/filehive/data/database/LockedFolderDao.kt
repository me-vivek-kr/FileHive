package com.viv3k.filehive.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedFolderDao {
    @Query("SELECT * FROM locked_folders ORDER BY lockedTime DESC")
    fun getAllLockedFolders(): Flow<List<LockedFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockedFolder(folder: LockedFolderEntity): Long

    @Delete
    suspend fun deleteLockedFolder(folder: LockedFolderEntity)

    @Query("SELECT * FROM locked_folders WHERE originalPath = :path LIMIT 1")
    suspend fun getLockedFolderByOriginalPath(path: String): LockedFolderEntity?

    @Query("SELECT * FROM locked_folders WHERE vaultPath = :path LIMIT 1")
    suspend fun getLockedFolderByVaultPath(path: String): LockedFolderEntity?
}
