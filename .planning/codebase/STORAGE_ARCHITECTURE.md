# Storage Architecture

> Scoped storage, MediaStore, SAF, file indexing, and Android version compatibility for FileHive (API 31–36).

---

## 1. Storage Landscape

### 1.1 FileHive's Storage Requirements

| Requirement | Storage API | Why |
|------------|-------------|-----|
| Browse all files/folders | `MANAGE_EXTERNAL_STORAGE` | Full filesystem traversal |
| Read file metadata | `File` API (legacy) | Fast directory listing |
| Create/delete files | `File` API + MediaStore fallback | Direct IO for managed storage |
| Recycle bin | `File.renameTo()` to `.FileHiveBin/` | Custom recycle with metadata |
| Vault (lock folders) | `File.renameTo()` to `.vault/` | Move to app-private directory |
| Display images/videos | Coil 3 + `File` URI | Direct file path loading |
| Search files | Custom file tree traversal | FileHive's own indexing |
| Storage stats | `StorageStatsManager` + `StatFs` | Space usage per volume |

### 1.2 Android Storage Model (API 31+)

```
┌──────────────────────────────────────────────────────────────┐
│                    EXTERNAL STORAGE                           │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Shared Storage (MediaStore)                           │  │
│  │  /storage/emulated/0/                                  │  │
│  │  ├── DCIM/          ← Images (MediaStore.Images)       │  │
│  │  ├── Pictures/      ← Images                          │  │
│  │  ├── Movies/        ← Videos (MediaStore.Video)        │  │
│  │  ├── Music/         ← Audio (MediaStore.Audio)         │  │
│  │  ├── Download/      ← Downloads (MediaStore.Downloads) │  │
│  │  ├── Documents/     ← Documents                        │  │
│  │  └── ...                                               │  │
│  └────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  App-Specific External                                 │  │
│  │  /storage/emulated/0/Android/data/com.viv3k.filehive/  │  │
│  │  └── files/                                            │  │
│  │      └── .vault/    ← FileHive vault (current)         │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│                    INTERNAL STORAGE                           │
│  /data/data/com.viv3k.filehive/                              │
│  ├── databases/                                              │
│  │   └── filehive_database    ← Room DB                     │
│  ├── shared_prefs/                                           │
│  │   └── secure_vault_prefs   ← EncryptedSharedPreferences  │
│  └── files/                                                  │
│      └── .vault/              ← Recommended vault location   │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Permission Strategy

### 2.1 FileHive Permission Model

```kotlin
// AndroidManifest.xml
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

// FileHive targets API 31+ (minSdk = 31), so:
// - READ_EXTERNAL_STORAGE is NOT needed (auto-granted for MANAGE_EXTERNAL_STORAGE)
// - WRITE_EXTERNAL_STORAGE is ignored on API 30+
// - MANAGE_EXTERNAL_STORAGE grants full access to shared storage
```

### 2.2 Permission Check Flow

```
App Launch
    │
    ▼
Check Environment.isExternalStorageManager()
    │
    ├── TRUE → Full access granted → Show file browser
    │
    └── FALSE → Show StoragePermissionGate composable
                 │
                 ▼
              User taps "Grant Access"
                 │
                 ▼
              ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                 │
                 ▼
              System settings → Toggle FileHive → On
                 │
                 ▼
              Return to app → Recheck → TRUE ✅
```

### 2.3 Current Implementation

```kotlin
// Current: StoragePermissionGate wraps AppNavigation
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoragePermissionGate {
                AppNavigation()
            }
        }
    }
}
```

---

## 3. MediaStore Strategy

### 3.1 When to Use MediaStore vs File API

| Operation | API | Reason |
|-----------|-----|--------|
| Browse folders | `File` API | `MANAGE_EXTERNAL_STORAGE` permits it; faster than MediaStore queries |
| Display media thumbnails | `ContentResolver` + Coil | Standard Coil handles content:// URIs |
| Get media metadata | `File` API | Direct access available; no need for cursor queries |
| Notify system of new files | `MediaScannerConnection` | Update MediaStore index after file operations |
| Inter-app sharing | `FileProvider` / content:// URIs | Required for sharing via Intent |
| Future cloud uploads | MediaStore query | Query specific media types efficiently |

### 3.2 MediaStore Scanner Integration

```kotlin
// ✅ Always notify MediaStore after file operations
class MediaStoreNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanFile(file: File) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            null
        ) { path, uri ->
            // File indexed by MediaStore
        }
    }

    fun scanDirectory(directory: File) {
        directory.walkTopDown()
            .filter { it.isFile }
            .toList()
            .let { files ->
                MediaScannerConnection.scanFile(
                    context,
                    files.map { it.absolutePath }.toTypedArray(),
                    null, null
                )
            }
    }

    // After delete — notify MediaStore to remove entry
    fun notifyDeleted(file: File) {
        val contentUri = MediaStore.Files.getContentUri("external")
        context.contentResolver.delete(
            contentUri,
            "${MediaStore.Files.FileColumns.DATA} = ?",
            arrayOf(file.absolutePath)
        )
    }
}
```

---

## 4. SAF (Storage Access Framework) Support

### 4.1 SAF Use Cases for FileHive

| Use Case | SAF API | When |
|----------|---------|------|
| Access SD card / USB OTG | `ACTION_OPEN_DOCUMENT_TREE` | User browses external volumes |
| Export files to user-chosen location | `ACTION_CREATE_DOCUMENT` | "Save As" functionality |
| Import from other apps | `ACTION_OPEN_DOCUMENT` | Pick file from other providers |
| Persist access to tree | `takePersistableUriPermission()` | Remember SD card access |

### 4.2 SAF Integration

```kotlin
class SafDocumentProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ✅ Request access to an entire directory tree (SD card, USB)
    fun createOpenTreeIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
    }

    // ✅ Persist URI permission across reboots
    fun persistTreeUri(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    // ✅ List children of a SAF directory
    fun listChildren(treeUri: Uri): List<SafFileItem> {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        return context.contentResolver.query(childrenUri, projection, null, null, null)
            ?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(SafFileItem(
                            documentId = cursor.getString(0),
                            name = cursor.getString(1),
                            size = cursor.getLong(2),
                            lastModified = cursor.getLong(3),
                            mimeType = cursor.getString(4),
                            isDirectory = cursor.getString(4) == DocumentsContract.Document.MIME_TYPE_DIR
                        ))
                    }
                }
            } ?: emptyList()
    }
}
```

---

## 5. Scoped Storage Handling

### 5.1 FileHive's Scoped Storage Compatibility

Since `minSdk = 31`, FileHive operates entirely in the scoped storage world. The `MANAGE_EXTERNAL_STORAGE` permission provides broad access, but some areas are still restricted:

| Path | Access Level | Notes |
|------|-------------|-------|
| `/storage/emulated/0/` | Full read/write | Via `MANAGE_EXTERNAL_STORAGE` |
| `/storage/emulated/0/Android/data/` | **Blocked** (API 33+) | Other apps' data directories |
| `/data/data/com.viv3k.filehive/` | Full | App-private internal storage |
| SD card / USB OTG | **SAF required** | `ACTION_OPEN_DOCUMENT_TREE` |
| `/storage/emulated/0/Android/data/com.viv3k.filehive/` | Full | Own app-specific external |

### 5.2 Path Resolution Strategy

```kotlin
object PathResolver {
    // ✅ Get primary storage root
    fun getPrimaryStorageRoot(): File {
        return Environment.getExternalStorageDirectory()
    }

    // ✅ Get all available storage volumes
    fun getStorageVolumes(context: Context): List<StorageVolumeInfo> {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return storageManager.storageVolumes.mapNotNull { volume ->
            volume.directory?.let { dir ->
                StorageVolumeInfo(
                    path = dir.absolutePath,
                    description = volume.getDescription(context) ?: "Unknown",
                    isPrimary = volume.isPrimary,
                    isRemovable = volume.isRemovable,
                    isEmulated = volume.isEmulated,
                    totalBytes = getTotalSpace(dir),
                    freeBytes = getFreeSpace(dir)
                )
            }
        }
    }

    // ✅ Check if path is accessible
    fun isPathAccessible(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.canRead()
    }

    // ✅ Resolve Android/data restriction
    fun isRestrictedPath(path: String): Boolean {
        val androidDataPattern = Regex("""/storage/[^/]+/Android/(data|obb)/(?!com\.viv3k\.filehive)""")
        return androidDataPattern.containsMatchIn(path)
    }
}
```

---

## 6. File Indexing Strategy

### 6.1 Current Approach

FileHive uses an in-memory `ConcurrentHashMap`-based cache (`FolderStatsCache`) for folder metadata:

```kotlin
// Current: FolderStatsCache.kt
object FolderStatsCache {
    private val cache = ConcurrentHashMap<String, CachedStats>()

    suspend fun getStats(root: File): Triple<Int, Long, Long> {
        // Check lastModified for invalidation
        // Recompute on IO dispatcher if stale
    }
}
```

### 6.2 Recommended: Tiered Indexing Architecture

```
┌──────────────────────────────────────────────────────────┐
│                   INDEXING TIERS                          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Tier 1: In-Memory Cache (Current)                       │
│  ├── ConcurrentHashMap<path, stats>                      │
│  ├── Invalidation: lastModified comparison               │
│  ├── Lifetime: Process lifetime                          │
│  └── Use: Hot folders being actively browsed             │
│                                                          │
│  Tier 2: Room Database Cache (Current partial)           │
│  ├── folder_cache table: path, sizeBytes, fileCount      │
│  ├── Invalidation: lastModified in entity                │
│  ├── Lifetime: Persistent across app restarts            │
│  └── Use: Previously visited folders                     │
│                                                          │
│  Tier 3: Full-Text Search Index (Planned)                │
│  ├── FTS4 virtual table or Room FTS entity               │
│  ├── Index: filename, extension, parent path             │
│  ├── Background: WorkManager periodic re-index           │
│  └── Use: Global search across entire filesystem         │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 6.3 Full-Text Search Index Design

```kotlin
// ✅ FTS entity for fast file search
@Entity(tableName = "file_index")
data class FileIndexEntity(
    @PrimaryKey val path: String,
    val name: String,
    val extension: String,
    val parentPath: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val mimeType: String?,
    val indexedAt: Long = System.currentTimeMillis()
)

// FTS virtual table for search
@Fts4(contentEntity = FileIndexEntity::class)
@Entity(tableName = "file_index_fts")
data class FileIndexFts(
    val name: String,
    val extension: String,
    val parentPath: String
)

@Dao
interface FileIndexDao {
    @Query("""
        SELECT file_index.* FROM file_index
        JOIN file_index_fts ON file_index.rowid = file_index_fts.rowid
        WHERE file_index_fts MATCH :query
        ORDER BY file_index.lastModified DESC
        LIMIT :limit
    """)
    suspend fun search(query: String, limit: Int = 100): List<FileIndexEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(files: List<FileIndexEntity>)

    @Query("DELETE FROM file_index WHERE parentPath = :parentPath")
    suspend fun deleteByParent(parentPath: String)

    @Query("SELECT MAX(indexedAt) FROM file_index")
    suspend fun getLastIndexTime(): Long?
}
```

### 6.4 Background Indexing Worker

```kotlin
class FileIndexWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val rootPaths = listOf(Environment.getExternalStorageDirectory())

        rootPaths.forEach { root ->
            indexDirectory(root, depth = 0, maxDepth = 10)
        }

        return Result.success()
    }

    private suspend fun indexDirectory(dir: File, depth: Int, maxDepth: Int) {
        if (depth > maxDepth) return

        val files = dir.listFiles() ?: return
        val entities = files.map { file ->
            FileIndexEntity(
                path = file.absolutePath,
                name = file.name,
                extension = file.extension,
                parentPath = file.parent ?: "",
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory,
                mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(file.extension)
            )
        }

        // Batch insert
        fileIndexDao.upsertAll(entities)

        // Recurse into subdirectories
        files.filter { it.isDirectory && !it.name.startsWith(".") }
            .forEach { indexDirectory(it, depth + 1, maxDepth) }
    }
}

// Schedule periodic indexing
fun scheduleFileIndexing(context: Context) {
    val request = PeriodicWorkRequestBuilder<FileIndexWorker>(
        repeatInterval = 6, repeatIntervalTimeUnit = TimeUnit.HOURS
    )
        .setConstraints(Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "file_index",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
```

---

## 7. Cache Management

### 7.1 Cache Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                     CACHE LAYERS                                │
├─────────────────┬──────────────┬───────────────────────────────┤
│  Memory Cache   │  Disk Cache  │  Database Cache               │
│  (Coil, Stats)  │  (Coil)     │  (Room folder_cache)          │
├─────────────────┼──────────────┼───────────────────────────────┤
│  Thumbnails:    │  Thumbnails: │  Folder metadata:             │
│  ~50MB limit    │  ~250MB      │  path, size, fileCount,       │
│  LRU eviction   │  LRU evict   │  lastModified, viewMode       │
│                 │              │                               │
│  Folder stats:  │              │  Locked folder records:       │
│  ConcurrentHash │              │  originalPath, vaultPath,     │
│  Map (~5MB)     │              │  folderName, lockedTime       │
├─────────────────┼──────────────┼───────────────────────────────┤
│  Eviction:      │  Eviction:   │  Eviction:                    │
│  Process death  │  Manual/     │  Manual per-entry or          │
│                 │  size limit  │  by lastModified staleness    │
└─────────────────┴──────────────┴───────────────────────────────┘
```

### 7.2 Cache Invalidation Rules

```kotlin
object CachePolicy {
    // Memory cache — invalidate when folder content changes
    const val MEMORY_CACHE_MAX_AGE_MS = 5 * 60 * 1000L // 5 minutes

    // Disk cache — managed by Coil
    const val COIL_DISK_CACHE_SIZE = 250L * 1024 * 1024 // 250MB

    // Database cache — invalidate by comparing lastModified
    const val DB_CACHE_STALE_THRESHOLD_MS = 30 * 60 * 1000L // 30 minutes

    // File index — full re-index interval
    const val INDEX_REFRESH_HOURS = 6L
}

// ✅ Smart invalidation strategy
class CacheManager @Inject constructor(
    private val folderStatsCache: FolderStatsCache,
    private val folderCacheDao: FolderCacheDao,
    private val coilImageLoader: ImageLoader
) {
    fun invalidateFolder(path: String) {
        folderStatsCache.invalidate(path)
        // DB cache updated on next access
    }

    fun invalidateAll() {
        folderStatsCache.clear()
        coilImageLoader.memoryCache?.clear()
    }

    suspend fun trimDiskCache() {
        coilImageLoader.diskCache?.let { diskCache ->
            // Coil manages LRU automatically, but we can force trim
        }
    }
}
```

---

## 8. Folder Scanning Architecture

### 8.1 Current Approach

```kotlin
// Current: computeFolderStats.kt — recursive file tree walk
fun computeFolderStats(root: File): Triple<Int, Long, Long> {
    var fileCount = 0
    var totalSize = 0L
    var latestModified = 0L

    root.walkTopDown().forEach { file ->
        if (file.isFile) {
            fileCount++
            totalSize += file.length()
            if (file.lastModified() > latestModified) {
                latestModified = file.lastModified()
            }
        }
    }

    return Triple(fileCount, totalSize, latestModified)
}
```

### 8.2 Recommended: Optimized Scanner

```kotlin
class FileSystemScanner @Inject constructor() {

    // ✅ Breadth-first scan with depth limit and cancellation
    suspend fun scanDirectory(
        root: File,
        maxDepth: Int = 1,
        includeHidden: Boolean = false
    ): ScanResult = withContext(Dispatchers.IO) {
        val items = mutableListOf<FileItem>()
        var totalSize = 0L
        var fileCount = 0
        var dirCount = 0
        var latestModified = 0L

        val children = root.listFiles() ?: return@withContext ScanResult.Empty

        for (file in children) {
            ensureActive() // Support coroutine cancellation

            if (!includeHidden && file.isHidden) continue

            val item = FileItem(
                name = file.name,
                path = file.absolutePath,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory,
                extension = file.extension.lowercase(),
                childCount = if (file.isDirectory) file.listFiles()?.size ?: 0 else 0
            )

            items.add(item)

            if (file.isFile) {
                fileCount++
                totalSize += file.length()
            } else {
                dirCount++
            }

            if (file.lastModified() > latestModified) {
                latestModified = file.lastModified()
            }
        }

        ScanResult(
            items = items,
            fileCount = fileCount,
            dirCount = dirCount,
            totalSize = totalSize,
            latestModified = latestModified
        )
    }

    // ✅ Progressive loading for large directories
    fun scanDirectoryFlow(
        root: File,
        batchSize: Int = 50
    ): Flow<List<FileItem>> = flow {
        val children = root.listFiles() ?: return@flow

        children.asSequence()
            .chunked(batchSize)
            .forEach { batch ->
                val items = batch.map { it.toFileItem() }
                emit(items)
            }
    }.flowOn(Dispatchers.IO)
}

data class ScanResult(
    val items: List<FileItem>,
    val fileCount: Int,
    val dirCount: Int,
    val totalSize: Long,
    val latestModified: Long
) {
    companion object {
        val Empty = ScanResult(emptyList(), 0, 0, 0L, 0L)
    }
}
```

---

## 9. Android Version Compatibility Strategy

### 9.1 API Level Feature Matrix

| Feature | API 31 (12) | API 33 (13) | API 34 (14) | API 35 (15) | API 36 (16) |
|---------|-------------|-------------|-------------|-------------|-------------|
| MANAGE_EXTERNAL_STORAGE | ✅ | ✅ | ✅ | ✅ | ✅ |
| Scoped storage enforced | ✅ | ✅ | ✅ | ✅ | ✅ |
| Android/data blocked | ❌ | ✅ | ✅ | ✅ | ✅ |
| Photo picker | ❌ | ✅ | ✅ | ✅ | ✅ |
| Partial media permissions | ❌ | ✅ | ✅ | ✅ | ✅ |
| Predictive back | ❌ | ❌ | ✅ | ✅ | ✅ |
| File path access (deprecated) | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ |
| Edge-to-edge enforced | ❌ | ❌ | ❌ | ✅ | ✅ |

### 9.2 Version-Aware Code Pattern

```kotlin
object StorageCompat {

    fun getAccessiblePaths(context: Context): List<String> {
        val paths = mutableListOf<String>()

        // Primary storage — always accessible with MANAGE_EXTERNAL_STORAGE
        paths.add(Environment.getExternalStorageDirectory().absolutePath)

        // Additional volumes
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        storageManager.storageVolumes.forEach { volume ->
            volume.directory?.absolutePath?.let { paths.add(it) }
        }

        return paths
    }

    fun isAndroidDataAccessible(): Boolean {
        // API 33+ blocks access to Android/data of other apps
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    fun shouldShowPhotoPicker(): Boolean {
        // Photo picker available on API 33+
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}
```

---

## 10. Recycle Bin Architecture

### 10.1 Current Implementation

```kotlin
// Current: FileRepository.kt
object FileRepository {
    private val RECYCLE_BIN_PATH = File(
        Environment.getExternalStorageDirectory(), ".FileHiveBin"
    )

    fun deleteFile(file: File, isPermanent: Boolean): Boolean {
        return if (isPermanent) deletePermanently(file)
        else moveToRecycleBin(file)
    }

    private fun moveToRecycleBin(file: File): Boolean {
        val timestamp = System.currentTimeMillis()
        val newName = "${file.name}_$timestamp"
        val destFile = File(RECYCLE_BIN_PATH, newName)
        val success = file.renameTo(destFile)
        if (success) {
            // Store original path in .repo sidecar file
            val metaFile = File(RECYCLE_BIN_PATH, "$newName.repo")
            metaFile.writeText(file.absolutePath)
        }
        return success
    }
}
```

### 10.2 Recommended: Room-Backed Recycle Bin

```kotlin
@Entity(tableName = "recycle_bin")
data class RecycleBinEntity(
    @PrimaryKey val binPath: String,        // Path in .FileHiveBin/
    val originalPath: String,                // Where to restore
    val originalName: String,                // Original filename
    val deletedAt: Long,                     // Timestamp
    val size: Long,                          // File size for UI
    val isDirectory: Boolean,                // Folder or file
    val autoDeleteAt: Long                   // Auto-purge timestamp (30 days)
)

@Dao
interface RecycleBinDao {
    @Query("SELECT * FROM recycle_bin ORDER BY deletedAt DESC")
    fun getAll(): Flow<List<RecycleBinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecycleBinEntity)

    @Delete
    suspend fun delete(entity: RecycleBinEntity)

    @Query("DELETE FROM recycle_bin WHERE autoDeleteAt < :now")
    suspend fun purgeExpired(now: Long = System.currentTimeMillis())
}
```

---

## 11. Storage Statistics

### 11.1 Current Implementation

```kotlin
// Current: getStorageStatsForPath.kt + StorageStats.kt
data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
)
```

### 11.2 Enhanced Statistics Provider

```kotlin
class StorageStatsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getVolumeStats(path: File): StorageStats {
        val statFs = StatFs(path.absolutePath)
        return StorageStats(
            totalBytes = statFs.totalBytes,
            freeBytes = statFs.freeBytes,
            usedBytes = statFs.totalBytes - statFs.freeBytes
        )
    }

    fun getCategoryBreakdown(): Flow<Map<FileCategory, Long>> = flow {
        val breakdown = mutableMapOf<FileCategory, Long>()

        val root = Environment.getExternalStorageDirectory()
        root.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val category = FileCategory.fromExtension(file.extension)
                breakdown[category] = (breakdown[category] ?: 0L) + file.length()
            }

        emit(breakdown)
    }.flowOn(Dispatchers.IO)
}

enum class FileCategory {
    IMAGES, VIDEOS, AUDIO, DOCUMENTS, ARCHIVES, APK, OTHER;

    companion object {
        fun fromExtension(ext: String): FileCategory = when (ext.lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg" -> IMAGES
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "3gp" -> VIDEOS
            "mp3", "wav", "flac", "aac", "ogg", "m4a" -> AUDIO
            "pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx", "csv" -> DOCUMENTS
            "zip", "rar", "7z", "tar", "gz" -> ARCHIVES
            "apk" -> APK
            else -> OTHER
        }
    }
}
```
