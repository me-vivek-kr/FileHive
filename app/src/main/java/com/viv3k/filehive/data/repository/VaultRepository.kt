package com.viv3k.filehive.data.repository

import android.content.Context
import com.viv3k.filehive.data.database.LockedFolderDao
import com.viv3k.filehive.data.database.LockedFolderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class VaultRepository(
    private val context: Context,
    private val lockedFolderDao: LockedFolderDao
) {

    private val vaultDir: File by lazy {
        val dir = File(context.getExternalFilesDir(null), ".vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    /**
     * Locks a folder by moving it to the hidden vault directory and
     * saving its metadata to the local database.
     */
    suspend fun lockFolder(originalFolder: File): Boolean = withContext(Dispatchers.IO) {
        if (!originalFolder.exists() || !originalFolder.isDirectory) return@withContext false

        // Check if already locked
        val existing = lockedFolderDao.getLockedFolderByOriginalPath(originalFolder.absolutePath)
        if (existing != null) return@withContext false

        val uniqueId = UUID.randomUUID().toString()
        val vaultFile = File(vaultDir, uniqueId)

        val success = originalFolder.renameTo(vaultFile)

        if (success) {
            val itemsCount = vaultFile.listFiles()?.size ?: 0
            val entity = LockedFolderEntity(
                originalPath = originalFolder.absolutePath,
                vaultPath = vaultFile.absolutePath,
                folderName = originalFolder.name,
                lockedTime = System.currentTimeMillis(),
                itemsCount = itemsCount
            )
            lockedFolderDao.insertLockedFolder(entity)
            true
        } else {
            false
        }
    }

    /**
     * Unlocks a folder by moving it back to its original location and
     * removing its metadata from the database.
     */
    suspend fun unlockFolder(entity: LockedFolderEntity): Boolean = withContext(Dispatchers.IO) {
        val vaultFile = File(entity.vaultPath)
        val originalFile = File(entity.originalPath)

        if (!vaultFile.exists()) {
            // If the folder is missing from the vault, clean up the DB
            lockedFolderDao.deleteLockedFolder(entity)
            return@withContext false
        }

        // Ensure parent of original exists
        originalFile.parentFile?.let {
            if (!it.exists()) it.mkdirs()
        }

        var destinationFile = originalFile

        // If a folder already exists at the destination, append a timestamp
        if (destinationFile.exists()) {
            destinationFile = File(
                destinationFile.parentFile,
                "${destinationFile.name}_restored_${System.currentTimeMillis()}"
            )
        }

        val success = vaultFile.renameTo(destinationFile)

        if (success) {
            lockedFolderDao.deleteLockedFolder(entity)
            true
        } else {
            false
        }
    }

    fun getAllLockedFolders() = lockedFolderDao.getAllLockedFolders()
}
