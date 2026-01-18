package com.viv3k.filehive.data.repository

import android.os.Environment
import java.io.File

object FileRepository {


    private val RECYCLE_BIN_PATH = File(Environment.getExternalStorageDirectory(), ".FileHiveBin")

    init {
        // Ensure the bin directory exists
        if (!RECYCLE_BIN_PATH.exists()) {
            RECYCLE_BIN_PATH.mkdirs()
        }
    }

    /**
     * Deletes a file based on the isPermanent flag.
     * Returns TRUE if successful.
     */
    fun deleteFile(file: File, isPermanent: Boolean): Boolean {
        return if (isPermanent) {
            deletePermanently(file)
        } else {
            moveToRecycleBin(file)
        }
    }

    private fun deletePermanently(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun moveToRecycleBin(file: File): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            val newName = "${file.name}_$timestamp"
            val destFile = File(RECYCLE_BIN_PATH, newName)

            val originalPath = file.absolutePath

            val success = file.renameTo(destFile)

            if (success) {
                val metaFile = File(RECYCLE_BIN_PATH,"$newName.repo")
                metaFile.writeText(originalPath)
            }

            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}