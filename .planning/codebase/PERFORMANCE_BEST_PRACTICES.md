# Performance Best Practices

> Compose optimization, file loading, Room tuning, coroutine management, and startup performance for FileHive.

---

## 1. Compose Optimization

### 1.1 Recomposition Prevention

```kotlin
// ❌ PROBLEM: Unstable lambda — causes recomposition of child
@Composable
fun FolderScreen(viewModel: FolderViewModel) {
    FileList(
        onDelete = { file -> viewModel.deleteFile(file) } // New lambda every recomposition
    )
}

// ✅ FIX: Use method reference or remember the lambda
@Composable
fun FolderScreen(viewModel: FolderViewModel) {
    val onDelete = remember<(FileItem) -> Unit> { { file -> viewModel.deleteFile(file) } }
    FileList(onDelete = onDelete)
}

// ✅ BEST: Use method reference when possible
FileList(onDelete = viewModel::deleteFile)
```

### 1.2 Stability Annotations

```kotlin
// ✅ Mark immutable classes as @Immutable for Compose stability
@Immutable
data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val extension: String = ""
)

// ✅ Mark collections as @Stable when wrapped
@Stable
class FileListState(
    val files: List<FileItem>,
    val isLoading: Boolean
)

// ✅ Use kotlinx.collections.immutable for truly immutable collections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class FolderUiState(
    val files: ImmutableList<FileItem> = persistentListOf()
)
```

### 1.3 Derived State

```kotlin
// ❌ PROBLEM: Recalculated on every recomposition
@Composable
fun FolderTopBar(files: List<FileItem>) {
    val totalSize = files.sumOf { it.size } // Runs every recomposition
    Text("Total: ${formatSize(totalSize)}")
}

// ✅ FIX: Use derivedStateOf for computed values
@Composable
fun FolderTopBar(files: List<FileItem>) {
    val totalSize by remember(files) {
        derivedStateOf { files.sumOf { it.size } }
    }
    Text("Total: ${formatSize(totalSize)}")
}
```

### 1.4 Key-Based Recomposition Control

```kotlin
// ❌ PROBLEM: No key — entire list recomposes on change
LazyColumn {
    items(files) { file -> FileRow(file) }
}

// ✅ FIX: Stable keys — only changed items recompose
LazyColumn {
    items(
        items = files,
        key = { file -> file.path }, // Stable, unique identifier
        contentType = { file -> if (file.isDirectory) "folder" else "file" }
    ) { file ->
        FileRow(file = file)
    }
}
```

---

## 2. LazyColumn Optimization

### 2.1 FileHive-Specific List Performance

```kotlin
@Composable
fun FileListView(
    files: List<FileItem>,
    onFileClick: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = files,
            key = { it.path },
            contentType = { if (it.isDirectory) "directory" else "file" }
        ) { file ->
            // ✅ Each item must be its own composable — not inline
            FileRowItem(
                file = file,
                onClick = { onFileClick(file) }
            )
        }
    }
}

// ✅ Separate composable for list items — enables independent recomposition
@Composable
private fun FileRowItem(
    file: FileItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Heavy composables (thumbnails) should be separate
    Row(modifier = modifier.clickable(onClick = onClick)) {
        FileThumbnail(file = file) // Separate composable for image loading
        FileInfo(file = file)     // Separate composable for text
    }
}
```

### 2.2 Thumbnail Loading Optimization

```kotlin
// ✅ Coil with proper sizing to avoid oversized bitmaps
@Composable
fun FileThumbnail(
    file: FileItem,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(File(file.path))
            .size(48.dp.toPx()) // Request exact size — no oversized bitmaps
            .crossfade(true)
            .memoryCacheKey(file.path)
            .diskCacheKey(file.path)
            .placeholder(R.drawable.ic_file_placeholder)
            .error(R.drawable.ic_file_error)
            .build(),
        contentDescription = file.name,
        modifier = modifier.size(48.dp),
        contentScale = ContentScale.Crop
    )
}

// ✅ Prefetch thumbnails for visible + upcoming items
@Composable
fun PrefetchingFileList(
    files: List<FileItem>,
    listState: LazyListState
) {
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    // Prefetch next 10 items beyond visible range
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val start = listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size
        val end = minOf(start + 10, files.size)

        for (i in start until end) {
            val file = files[i]
            if (file.isImageOrVideo()) {
                val request = ImageRequest.Builder(context)
                    .data(File(file.path))
                    .size(48)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }
}
```

### 2.3 Infinite Scrolling for Large Directories

```kotlin
// ✅ For directories with 10,000+ files — load in batches
@Composable
fun PaginatedFileList(
    viewModel: FolderViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Detect when user scrolls near the end
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 15 // Load more when 15 items from end
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextBatch()
        }
    }

    LazyColumn(state = listState, modifier = modifier) {
        // ... items
    }
}
```

---

## 3. File Loading Optimization

### 3.1 Background File Scanning

```kotlin
// ✅ Progressive loading — show results as they arrive
class FolderViewModel @Inject constructor(
    private val scanner: FileSystemScanner
) : ViewModel() {

    fun loadFolder(path: String) {
        viewModelScope.launch {
            _state.value = FolderUiState.Loading

            // Progressive: emit batches as they're scanned
            scanner.scanDirectoryFlow(path, batchSize = 50)
                .scan(emptyList<FileItem>()) { accumulated, batch ->
                    accumulated + batch
                }
                .collect { allFiles ->
                    _state.value = FolderUiState.Success(
                        files = allFiles,
                        fileCount = allFiles.size,
                        totalSize = allFiles.sumOf { it.size }
                    )
                }
        }
    }
}
```

### 3.2 Folder Stats Caching

```kotlin
// ✅ Current FolderStatsCache already uses ConcurrentHashMap
// Improvement: Add LRU eviction and size limit
class FolderStatsCache @Inject constructor() {

    private val cache = object : LinkedHashMap<String, CachedStats>(100, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedStats>): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }

    companion object {
        private const val MAX_CACHE_ENTRIES = 500
        private const val STALE_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes
    }

    @Synchronized
    suspend fun getStats(root: File): FolderStats {
        val path = root.absolutePath
        val currentLm = root.lastModified()
        val now = System.currentTimeMillis()

        val cached = cache[path]
        if (cached != null &&
            cached.observedLastModified == currentLm &&
            now - cached.timestampMs < STALE_THRESHOLD_MS
        ) {
            return cached.stats
        }

        val stats = withContext(Dispatchers.IO) {
            computeFolderStats(root)
        }

        cache[path] = CachedStats(stats, currentLm, now)
        return stats
    }
}
```

---

## 4. Room Optimization

### 4.1 Query Optimization

```kotlin
// ❌ BAD: Loading all columns when only name needed
@Query("SELECT * FROM folder_cache")
suspend fun getAll(): List<FolderCacheEntity>

// ✅ GOOD: Project only needed columns
@Query("SELECT path, fileCount FROM folder_cache WHERE path LIKE :parentPath || '%'")
suspend fun getChildStats(parentPath: String): List<PathAndCount>

data class PathAndCount(val path: String, val fileCount: Int)

// ✅ Use @Transaction for multi-query operations
@Transaction
suspend fun lockAndRecord(entity: LockedFolderEntity, cacheUpdate: FolderCacheEntity) {
    insertLockedFolder(entity)
    insertOrUpdate(cacheUpdate)
}
```

### 4.2 Database Write Optimization

```kotlin
// ❌ BAD: Insert one-by-one in a loop
files.forEach { file ->
    dao.insertOrUpdate(file.toCacheEntity()) // N separate transactions
}

// ✅ GOOD: Batch insert in single transaction
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertAll(entities: List<FolderCacheEntity>) // 1 transaction

// ✅ Use @Transaction for complex operations
@Transaction
@Query("DELETE FROM folder_cache WHERE path LIKE :parentPath || '%'")
suspend fun clearCacheForDirectory(parentPath: String)
```

### 4.3 Database Configuration

```kotlin
// ✅ Production database setup
Room.databaseBuilder(context, AppDatabase::class.java, "filehive_database")
    .addMigrations(MIGRATION_2_3, MIGRATION_3_4) // Never fallbackToDestructiveMigration in prod
    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // WAL for concurrent reads
    .setQueryExecutor(Executors.newFixedThreadPool(4)) // Parallel queries
    .build()
```

---

## 5. Coroutine Optimization

### 5.1 Dispatcher Usage

```kotlin
// ✅ Correct dispatcher for each operation type
// IO: File operations, database queries, network
withContext(Dispatchers.IO) { file.readBytes() }

// Default: CPU-intensive computations (sorting, filtering large lists)
withContext(Dispatchers.Default) { files.sortedBy { it.name } }

// Main: UI updates (StateFlow emissions are automatically main-safe)
_state.value = FolderUiState.Success(...) // Already on Main via StateFlow
```

### 5.2 Structured Concurrency

```kotlin
// ✅ Parallel operations with coroutineScope
suspend fun loadDashboard(): DashboardData = coroutineScope {
    val statsDeferred = async(Dispatchers.IO) { getStorageStats() }
    val lockedCountDeferred = async(Dispatchers.IO) { getLockedFolderCount() }
    val recentDeferred = async(Dispatchers.IO) { getRecentFiles() }

    DashboardData(
        stats = statsDeferred.await(),
        lockedCount = lockedCountDeferred.await(),
        recent = recentDeferred.await()
    )
}

// ✅ Cancellation support in long operations
suspend fun scanLargeDirectory(root: File) = withContext(Dispatchers.IO) {
    root.walkTopDown().forEach { file ->
        ensureActive() // Check for cancellation between files
        processFile(file)
    }
}
```

### 5.3 Flow Backpressure

```kotlin
// ✅ Conflate for rapid file system changes
fileSystemObserver.fileChanges
    .conflate() // Drop intermediate emissions
    .debounce(300) // Wait 300ms after last change
    .collect { change ->
        reloadFolder(change.path)
    }

// ✅ Buffer for producer-consumer imbalance
scanner.scanDirectoryFlow(path)
    .buffer(Channel.BUFFERED) // Default 64 item buffer
    .collect { batch -> processAndDisplay(batch) }
```

---

## 6. Startup Optimization

### 6.1 Cold Start Analysis

```
Current startup path:
1. Application.onCreate()     ← Nothing currently
2. MainActivity.onCreate()    ← enableEdgeToEdge() + setContent()
3. StoragePermissionGate      ← Permission check
4. AppNavigation              ← NavHost initialization
5. SplashScreen               ← Animation
6. HomeScreen                 ← Storage scan + UI render
```

### 6.2 Optimization Strategies

```kotlin
// ✅ Lazy initialization of expensive singletons
@Singleton
class AppDatabase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Database built lazily on first access
    val instance: RoomDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "filehive_database")
            .build()
    }
}

// ✅ Startup tasks on background thread
@HiltAndroidApp
class FileHiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Warmup on background thread — don't block main thread
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            // Pre-warm Room database (schema validation)
            appDatabase.folderCacheDao().getFolderCache("__warmup__")

            // Pre-populate Coil memory cache for common icons
            preloadCommonIcons()
        }
    }
}

// ✅ Use App Startup library for component initialization
class CoilInitializer : Initializer<ImageLoader> {
    override fun create(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.15) // 15% of app memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024) // 250MB
                    .build()
            }
            .crossfade(true)
            .build()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
```

### 6.3 Baseline Profiles

```kotlin
// ✅ Generate baseline profiles for critical paths
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(packageName = "com.viv3k.filehive") {
            // Cold start
            startActivityAndWait()

            // Navigate through critical paths
            device.findObject(By.text("Internal Storage")).click()
            device.waitForIdle()

            // Scroll file list
            device.findObject(By.scrollable(true))?.apply {
                scroll(Direction.DOWN, 3f)
            }
        }
    }
}
```

---

## 7. Memory Management

### 7.1 Bitmap Memory

```kotlin
// ✅ Coil configuration for optimal memory usage
val imageLoader = ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.15) // 15% of available RAM
            .build()
    }
    .components {
        add(VideoFrameDecoder.Factory()) // For video thumbnails
    }
    .build()

// ✅ Downsize images to display size
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(file)
        .size(Size(96, 96)) // Match display dimensions
        .scale(Scale.FILL)
        .build(),
    contentDescription = null
)
```

### 7.2 File List Memory

```kotlin
// ❌ PROBLEM: Holding entire file tree in memory
val allFiles = root.walkTopDown().toList() // Millions of files = OOM

// ✅ FIX: Use sequences and pagination
val firstBatch = root.listFiles()
    ?.asSequence()
    ?.take(100)
    ?.map { it.toFileItem() }
    ?.toList()
    ?: emptyList()

// ✅ FIX: Stream large directories
fun streamDirectory(root: File): Flow<FileItem> = flow {
    root.listFiles()?.forEach { file ->
        emit(file.toFileItem())
    }
}.flowOn(Dispatchers.IO)
```

### 7.3 Leak Prevention

```kotlin
// ❌ LEAK: Holding Activity context in singleton
object FolderStatsCache {
    private var context: Context? = null // Leaks Activity!
}

// ✅ FIX: Use Application context via Hilt
@Singleton
class FolderStatsCache @Inject constructor(
    @ApplicationContext private val context: Context // Application context
)

// ❌ LEAK: Anonymous callback holding outer reference
viewModelScope.launch {
    repository.observe(object : Callback {
        override fun onResult(data: Data) {
            // Holds reference to ViewModel → Activity
        }
    })
}

// ✅ FIX: Use Flow instead of callbacks
viewModelScope.launch {
    repository.observe().collect { data ->
        // Cancelled when viewModelScope is cancelled
    }
}
```

---

## 8. Background Processing Strategy

### 8.1 WorkManager for Long-Running Tasks

```kotlin
// ✅ File indexing — survives process death
class FileIndexWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val startPath = inputData.getString("root_path")
                ?: Environment.getExternalStorageDirectory().absolutePath

            indexDirectory(File(startPath))
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,
            createNotification("Indexing files...")
        )
    }
}
```

### 8.2 Task Selection Guide

| Task | API | Why |
|------|-----|-----|
| File indexing (6h periodic) | WorkManager | Survives process death, battery-aware |
| Recycle bin auto-purge (daily) | WorkManager | Scheduled, deferrable |
| Folder scan (user-triggered) | viewModelScope | Interactive, cancel on nav away |
| Thumbnail generation | Coil (built-in) | Library-managed background loading |
| Database vacuum | WorkManager | Periodic, maintenance task |
| File copy/move progress | Foreground Service | User-visible, long-running |

### 8.3 Foreground Service for File Operations

```kotlin
// ✅ Large file copy/move with progress notification
class FileOperationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sourceFile = intent?.getStringExtra("source") ?: return START_NOT_STICKY
        val destPath = intent.getStringExtra("destination") ?: return START_NOT_STICKY

        startForeground(NOTIFICATION_ID, createProgressNotification(0))

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            copyFileWithProgress(File(sourceFile), File(destPath)) { progress ->
                updateNotification(progress)
            }
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun copyFileWithProgress(
        source: File,
        dest: File,
        onProgress: (Int) -> Unit
    ) {
        val totalBytes = source.length()
        var copiedBytes = 0L

        source.inputStream().buffered().use { input ->
            dest.outputStream().buffered().use { output ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    copiedBytes += read
                    onProgress((copiedBytes * 100 / totalBytes).toInt())
                }
            }
        }
    }
}
```

---

## 9. Performance Monitoring Checklist

| Metric | Target | Tool |
|--------|--------|------|
| Cold start time | < 500ms to first frame | Android Studio Profiler |
| Frame render time | < 16ms (60fps) | Compose Layout Inspector |
| Recomposition count | Minimize skipped % > 80% | Compose Compiler metrics |
| Memory usage (idle) | < 80MB | Android Profiler |
| Memory usage (browsing) | < 150MB | Android Profiler |
| Folder load time (1K files) | < 200ms | Custom timing logs |
| Folder load time (10K files) | < 1s (progressive) | Custom timing logs |
| Database query time | < 50ms per query | Room query logging |
| Thumbnail load time | < 100ms per image | Coil listener logging |
| ANR rate | 0% | Play Console vitals |

---

## 10. Performance Anti-Patterns to Avoid

```kotlin
// ❌ Running expensive computation during composition
@Composable
fun FolderScreen(files: List<FileItem>) {
    val sorted = files.sortedBy { it.name } // Sorts on EVERY recomposition
}

// ❌ Creating objects in composition (causes recomposition)
@Composable
fun FileRow(file: FileItem) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy") // New object every recomposition
}

// ❌ Blocking the main thread
fun loadFiles(): List<File> {
    return File("/storage/emulated/0").listFiles()?.toList() ?: emptyList() // On main thread!
}

// ❌ Using Thread instead of coroutines
Thread {
    val files = scanDirectory(root)
    runOnUiThread { updateUI(files) } // Old-school, not lifecycle-aware
}.start()

// ❌ Ignoring key parameter in LazyColumn
LazyColumn {
    items(files) { file -> FileRow(file) } // No key = full list recomposition
}
```
