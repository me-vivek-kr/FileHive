package com.viv3k.filehive.data.storage

import java.io.File

/**
 * Compute stats for a folder:
 * - first = number of files (not counting directories)
 * - second = total size in bytes (sum of all files under `root`)
 * - third = latest lastModified timestamp among all files/dirs in the tree
 */
fun computeFolderStats(root: File): Triple<Int, Long, Long> {
    var fileCount = 0
    var totalSize = 0L
    var lastModified = root.lastModified()

    if (!root.exists()) return Triple(0, 0L, lastModified)

    val stack = ArrayDeque<File>()
    stack.add(root)

    while (stack.isNotEmpty()) {
        val dir = stack.removeFirst()
        val children = try {
            dir.listFiles()
        } catch (e: SecurityException) {
            // If we can't access this directory, skip it
            null
        } ?: continue

        for (c in children) {
            // update newest timestamp seen (both files and directories)
            val lm = c.lastModified()
            if (lm > lastModified) lastModified = lm

            if (c.isDirectory) {
                // push directory to traverse its children later
                stack.add(c)
                // If you want to count directories as items, increment a separate counter here.
            } else {
                // it's a file: count it and add its size
                fileCount++
                totalSize += c.length()
            }
        }
    }

    return Triple(fileCount, totalSize, lastModified)
}
