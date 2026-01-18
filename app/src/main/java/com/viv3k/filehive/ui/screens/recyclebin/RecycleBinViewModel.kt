package com.viv3k.filehive.ui.screens.recyclebin

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class representing bin
data class RealBinItem(
    val file: File,
    val displayName: String,
    val originalTimestamp: Long,
    val deletedTimeFormatted: String,
    val type: FileType,
    val absolutePath: String,
    val isFolder: Boolean
)

class RecycleBinViewModel : ViewModel() {

    private val _binItems = MutableStateFlow<List<RealBinItem>>(emptyList())
    val binItems = _binItems.asStateFlow()

    // Match the path used in FileRepository
    private val binPath = File(Environment.getExternalStorageDirectory(), ".FileHiveBin")

    // Define a default restore location
    private val fallbackRestorePath = File(Environment.getExternalStorageDirectory(), "Restored_FileHive")

    init {
        loadBinItems()
    }

    fun loadBinItems() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!binPath.exists()) {
                _binItems.value = emptyList()
                return@launch
            }

            val files = binPath.listFiles() ?: emptyArray()

            // Filter out the .repo files from the list, we only want actual files
            val validFiles = files.filter { !it.name.endsWith(".repo") }

            val items = validFiles.map { file ->
                // 1. Try to read the original path from the .repo file
                val metaFile = File(file.parent, "${file.name}.repo")
                val originalPath = if (metaFile.exists()) {
                    metaFile.readText() // Returns "/storage/emulated/0/dubox/test"
                } else {
                    "Unknown Location" // Fallback for old deleted files
                }

                // Parse filename logic
                val namePartsIndex = file.name.lastIndexOf('_')
                val (displayName, timestamp) = if (namePartsIndex != -1) {
                    val name = file.name.substring(0, namePartsIndex)
                    val timeStr = file.name.substring(namePartsIndex + 1)
                    val time = timeStr.toLongOrNull() ?: file.lastModified()
                    Pair(name, time)
                } else {
                    Pair(file.name, file.lastModified())
                }

                val type = if (file.isDirectory) FileType.ARCHIVE else determineFileType(file)

                RealBinItem(
                    file = file,
                    displayName = displayName,
                    originalTimestamp = timestamp,
                    deletedTimeFormatted = formatRelativeTime(timestamp),
                    type = type,
                    absolutePath = originalPath, // UI will now show the correct path!
                    isFolder = file.isDirectory
                )
            }.sortedByDescending { it.originalTimestamp }

            _binItems.value = items
        }
    }

    fun emptyBin() {
        viewModelScope.launch(Dispatchers.IO) {
            if (binPath.exists()) {
                binPath.deleteRecursively()
                binPath.mkdirs()
                loadBinItems()
            }
        }
    }

    fun restoreAll() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!binPath.exists()) return@launch

            val files = binPath.listFiles() ?: emptyArray()
            // Filter out .repo files, we process them via the main file
            val filesToRestore = files.filter { !it.name.endsWith(".repo") }

            filesToRestore.forEach { file ->
                // 1. Get Original Path from .repo
                val metaFile = File(file.parent, "${file.name}.repo")

                val destFile = if (metaFile.exists()) {
                    // Restore to EXACT original location
                    File(metaFile.readText())
                } else {
                    // Fallback logic if meta file is missing
                    val namePartsIndex = file.name.lastIndexOf('_')
                    val originalName = if (namePartsIndex != -1) file.name.substring(0, namePartsIndex) else file.name
                    if (!fallbackRestorePath.exists()) fallbackRestorePath.mkdirs()
                    File(fallbackRestorePath, originalName)
                }

                // 2. Ensure parent folder exists (in case user deleted the 'dubox' folder too)
                if (destFile.parentFile?.exists() == false) {
                    destFile.parentFile?.mkdirs()
                }

                // 3. Move File
                val moved = moveFileOrDir(file, destFile)

                // 4. If restore successful, delete the .repo file
                if (moved && metaFile.exists()) {
                    metaFile.delete()
                }
            }

            loadBinItems()
        }
    }

    fun restoreFile(item: RealBinItem){
        viewModelScope.launch(Dispatchers.IO){
            val file = item.file
            val metaFile = File(file.parent, "${file.name}.repo")

            val destFile = if(metaFile.exists()){
                //Restore to EXACT original location
                File(metaFile.readText())
            } else{
                val namePartsIndex = file.name.lastIndexOf('_')
                val originalName = if (namePartsIndex != -1) file.name.substring(0, namePartsIndex) else file.name
                if (!fallbackRestorePath.exists()) fallbackRestorePath.mkdirs()
                File(fallbackRestorePath, originalName)
            }

            //Ensure Parent Folder Exists
            if(destFile.parentFile?.exists() == false){
                destFile.parentFile?.mkdirs()
            }

            //Move File
            val moved = moveFileOrDir(file, destFile)

            //Cleanup
            if(moved && metaFile.exists()){
                metaFile.delete()
            }

            loadBinItems() //Refresh UI
        }
    }

    fun deleteFilePermanently(item: RealBinItem){
        viewModelScope.launch(Dispatchers.IO){
            val file = item.file
            val metaFile = File(file.parent, "${file.name}.repo")

            //Delete Actual File/Folder
            if(file.isDirectory) file.deleteRecursively() else file.delete()

            //Delete Meta File
            if(metaFile.exists()) metaFile.delete()

            loadBinItems() //Refresh UI
        }
    }



    private fun moveFileOrDir(source: File, dest: File): Boolean {
        // Handle name conflicts if file already exists at destination
        var finalDest = dest
        var counter = 1
        while (finalDest.exists()) {
            val parent = finalDest.parentFile
            val name = finalDest.nameWithoutExtension
            val ext = finalDest.extension
            val newName = if (ext.isNotEmpty()) "$name($counter).$ext" else "$name($counter)"
            finalDest = File(parent, newName)
            counter++
        }

        if (source.renameTo(finalDest)) return true

        return try {
            if (source.isDirectory) {
                source.copyRecursively(finalDest, overwrite = true)
            } else {
                source.copyTo(finalDest, overwrite = true)
            }
            if (source.isDirectory) source.deleteRecursively() else source.delete()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun determineFileType(file: File): FileType {
        if (file.isDirectory) return FileType.ARCHIVE // Treat folder as archive icon for now
        val ext = file.extension.lowercase()
        return when (ext) {
            in listOf("jpg", "jpeg", "png", "gif", "webp") -> FileType.IMAGE
            in listOf("mp4", "mkv", "avi", "mov") -> FileType.VIDEO
            in listOf("mp3", "wav", "aac", "flac") -> FileType.AUDIO
            in listOf("pdf", "doc", "docx", "txt") -> FileType.DOCUMENT
            in listOf("zip", "rar", "7z", "tar") -> FileType.ARCHIVE
            else -> FileType.DOCUMENT
        }
    }

    private fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days <= 0L -> "Deleted Today"
            days == 1L -> "Deleted Yesterday"
            days < 30 -> "Deleted $days days ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "Deleted on ${sdf.format(Date(timestamp))}"
            }
        }
    }
}