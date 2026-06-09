# Debugging Guide

> ADB commands, storage debugging, Compose inspection, Room debugging, coroutine tracing, and performance profiling for FileHive.

---

## 1. Common ADB Commands

### 1.1 App Management

```bash
# Install debug APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Uninstall (preserves data)
adb uninstall -k com.viv3k.filehive

# Uninstall (removes all data)
adb uninstall com.viv3k.filehive

# Force stop the app
adb shell am force-stop com.viv3k.filehive

# Clear all app data (Room DB, SharedPrefs, cache)
adb shell pm clear com.viv3k.filehive

# Launch the app
adb shell am start -n com.viv3k.filehive/.MainActivity

# Launch to specific screen (via deep link)
adb shell am start -a android.intent.action.VIEW \
  -d "filehive://folder//storage/emulated/0/DCIM" \
  com.viv3k.filehive
```

### 1.2 File System Inspection

```bash
# List vault directory
adb shell ls -la /storage/emulated/0/Android/data/com.viv3k.filehive/files/.vault/

# List recycle bin
adb shell ls -la /storage/emulated/0/.FileHiveBin/

# Check app internal storage (Room DB location)
adb shell run-as com.viv3k.filehive ls -la /data/data/com.viv3k.filehive/databases/

# Check EncryptedSharedPreferences
adb shell run-as com.viv3k.filehive ls -la /data/data/com.viv3k.filehive/shared_prefs/

# Copy Room database to local machine for inspection
adb shell run-as com.viv3k.filehive cp /data/data/com.viv3k.filehive/databases/filehive_database /sdcard/filehive_debug.db
adb pull /sdcard/filehive_debug.db ./

# Check storage permissions
adb shell appops get com.viv3k.filehive MANAGE_EXTERNAL_STORAGE

# Grant MANAGE_EXTERNAL_STORAGE via ADB
adb shell appops set com.viv3k.filehive MANAGE_EXTERNAL_STORAGE allow
```

### 1.3 Logs and Diagnostics

```bash
# FileHive logs only
adb logcat -s "FileHive" "*:E"

# Compose recomposition logs
adb logcat | grep -i "recomposition"

# Room query logs (verbose)
adb logcat -s "ROOM" "*:D"

# ANR traces
adb shell cat /data/anr/traces.txt

# Memory info
adb shell dumpsys meminfo com.viv3k.filehive

# Activity stack
adb shell dumpsys activity activities | grep -A 5 "com.viv3k.filehive"

# Storage stats
adb shell df /storage/emulated/0

# CPU usage
adb shell top -n 1 | grep filehive

# Network connections (for future cloud features)
adb shell dumpsys connectivity | grep -A 3 "com.viv3k.filehive"
```

### 1.4 Screen Recording & Screenshots

```bash
# Take screenshot
adb shell screencap /sdcard/debug_screenshot.png
adb pull /sdcard/debug_screenshot.png

# Record screen (max 180 seconds)
adb shell screenrecord /sdcard/debug_recording.mp4
# Ctrl+C to stop
adb pull /sdcard/debug_recording.mp4

# Record with overlay showing touches
adb shell screenrecord --show-touches /sdcard/debug_recording.mp4
```

---

## 2. Storage Debugging

### 2.1 Permission Issues

```bash
# Check if MANAGE_EXTERNAL_STORAGE is granted
adb shell appops get com.viv3k.filehive MANAGE_EXTERNAL_STORAGE
# Expected output: MANAGE_EXTERNAL_STORAGE: allow

# Check all permissions
adb shell dumpsys package com.viv3k.filehive | grep -A 20 "granted=true"

# Reset permissions
adb shell pm reset-permissions com.viv3k.filehive
```

### 2.2 File Operation Debugging

```kotlin
// ✅ Add debug logging to file operations
object FileDebugger {
    private const val TAG = "FileHive:Storage"

    fun logFileOperation(operation: String, file: File, success: Boolean) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$operation: ${file.absolutePath} " +
                "| exists=${file.exists()} " +
                "| canRead=${file.canRead()} " +
                "| canWrite=${file.canWrite()} " +
                "| size=${file.length()} " +
                "| success=$success")
        }
    }

    fun logDirectoryScan(dir: File) {
        if (BuildConfig.DEBUG) {
            val files = dir.listFiles()
            Log.d(TAG, "Scan: ${dir.absolutePath} " +
                "| exists=${dir.exists()} " +
                "| children=${files?.size ?: "null"} " +
                "| canRead=${dir.canRead()}")
        }
    }

    fun logVaultOperation(operation: String, original: String, vault: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Vault $operation: $original → $vault")
        }
    }
}
```

### 2.3 Common Storage Issues

| Symptom | Cause | Fix |
|---------|-------|-----|
| `listFiles()` returns null | Permission not granted or path invalid | Check `MANAGE_EXTERNAL_STORAGE` |
| `renameTo()` returns false | Cross-filesystem move (SD card) | Use copy + delete instead |
| Empty folder shown with files | Cache stale, `lastModified` unchanged | Force invalidate + rescan |
| Vault folder visible in gallery | Missing `.nomedia` file | Add `.nomedia` to vault dir |
| Files disappear after reboot | Vault path on external SD ejected | Verify path existence on startup |

---

## 3. Compose Debugging

### 3.1 Recomposition Tracking

```kotlin
// ✅ Debug composable — shows recomposition count
@Composable
fun RecompositionTracker(label: String) {
    if (BuildConfig.DEBUG) {
        val count = remember { mutableIntStateOf(0) }
        SideEffect { count.intValue++ }
        Log.d("Recomposition", "$label: ${count.intValue} times")
    }
}

// Usage — place inside any composable to track
@Composable
fun FolderScreen(...) {
    RecompositionTracker("FolderScreen")
    // ... rest of composable
}
```

### 3.2 Compose Compiler Reports

```bash
# Generate Compose compiler metrics
# Add to build.gradle.kts:
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

# Build to generate reports
./gradlew assembleDebug

# Check reports at:
# app/build/compose_compiler/
# - app_debug-composables.txt    ← Composable stability info
# - app_debug-classes.txt        ← Class stability analysis
# - app_debug-module.json        ← Module-level metrics
```

### 3.3 Layout Inspector (Android Studio)

```
1. Run app on device/emulator
2. View → Tool Windows → Layout Inspector
3. Select FileHive process
4. Navigate to the screen to inspect
5. Click composables to see:
   - Recomposition count
   - Skip count (higher is better)
   - Parameters that triggered recomposition
```

### 3.4 Common Compose Issues

| Issue | Diagnosis | Fix |
|-------|-----------|-----|
| Janky scrolling in file list | Missing `key` parameter in `LazyColumn` | Add `key = { file.path }` |
| Entire list recomposes on selection | Unstable list parameter | Use `ImmutableList` or `@Stable` |
| Thumbnail flicker | New `ImageRequest` each recomposition | `remember` the request or use `memoryCacheKey` |
| State loss on rotation | State in `remember` instead of ViewModel | Move to `StateFlow` in ViewModel |
| Nested scrolling conflict | `LazyColumn` inside `Column(scrollable)` | Use `LazyColumn` with headers instead |

---

## 4. Room Inspection

### 4.1 Database Inspector (Android Studio)

```
1. Run app on API 26+ device/emulator
2. View → Tool Windows → App Inspection
3. Select "Database Inspector" tab
4. Select com.viv3k.filehive process
5. Browse: filehive_database
   → folder_cache
   → locked_folders
6. Run live queries in the query editor
```

### 4.2 Debug Queries

```sql
-- Check all locked folders
SELECT * FROM locked_folders ORDER BY lockedTime DESC;

-- Verify vault path integrity (files still exist)
SELECT id, folderName, vaultPath,
    CASE WHEN vaultPath IS NOT NULL THEN 'CHECK_FS' ELSE 'MISSING' END as status
FROM locked_folders;

-- Find orphaned cache entries (folders that no longer exist)
SELECT path FROM folder_cache
WHERE path NOT IN (SELECT DISTINCT originalPath FROM locked_folders);

-- Cache statistics
SELECT
    COUNT(*) as total_entries,
    SUM(sizeBytes) as total_cached_size,
    MIN(lastModified) as oldest_cache,
    MAX(lastModified) as newest_cache
FROM folder_cache;

-- Database size
SELECT page_count * page_size as size_bytes FROM pragma_page_count(), pragma_page_size();
```

### 4.3 Room Debug Logging

```kotlin
// ✅ Enable Room query logging in debug builds
Room.databaseBuilder(context, AppDatabase::class.java, "filehive_database")
    .apply {
        if (BuildConfig.DEBUG) {
            // Log all queries
            setQueryCallback(RoomDatabase.QueryCallback { sqlQuery, bindArgs ->
                Log.d("Room", "Query: $sqlQuery | Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
        }
    }
    .build()
```

---

## 5. Coroutine Debugging

### 5.1 Debug Probes

```kotlin
// ✅ Enable coroutine debug agent in Application.onCreate()
if (BuildConfig.DEBUG) {
    System.setProperty("kotlinx.coroutines.debug", "on")
}
```

### 5.2 Coroutine Tracing

```kotlin
// ✅ Named coroutines for easier debugging
viewModelScope.launch(CoroutineName("loadFolder")) {
    // In stack traces: "loadFolder#1"
    loadFolder(path)
}

viewModelScope.launch(Dispatchers.IO + CoroutineName("deleteFile")) {
    deleteFile(file)
}
```

### 5.3 Common Coroutine Issues

| Issue | Symptom | Fix |
|-------|---------|-----|
| Job cancellation ignored | Work continues after nav away | Check `ensureActive()` in loops |
| Main thread blocked | ANR / frozen UI | Wrap in `withContext(Dispatchers.IO)` |
| StateFlow not updating | UI shows stale data | Check `MutableStateFlow` is being set |
| Race condition | Intermittent wrong state | Use `Mutex` or `Channel` |
| Memory leak | ViewModel not garbage collected | Don't hold Activity ref in coroutine |

### 5.4 Flow Debugging

```kotlin
// ✅ Debug flow operators
fun <T> Flow<T>.debugLog(tag: String): Flow<T> = this
    .onStart { Log.d(tag, "Flow started") }
    .onEach { value -> Log.d(tag, "Emit: $value") }
    .onCompletion { cause -> Log.d(tag, "Flow completed: $cause") }
    .catch { error -> Log.e(tag, "Flow error: ${error.message}", error) }

// Usage
viewModel.state
    .debugLog("FolderState")
    .collectAsStateWithLifecycle()
```

---

## 6. ANR Debugging

### 6.1 ANR Detection

```bash
# Pull ANR traces
adb pull /data/anr/traces.txt ./anr_traces.txt

# View with filtering
grep -A 50 "com.viv3k.filehive" ./anr_traces.txt
```

### 6.2 Common ANR Causes in FileHive

| Operation | Cause | Fix |
|-----------|-------|-----|
| `File.listFiles()` on main thread | Large directory (10K+ files) | Move to `Dispatchers.IO` |
| `computeFolderStats()` on main | Recursive file walk | Already on IO — verify call site |
| Room query on main thread | `allowMainThreadQueries()` | Remove; use `suspend` DAO functions |
| `EncryptedSharedPreferences.create()` | First-time key generation slow | Lazy init on background thread |
| `renameTo()` for large folders | Cross-partition move | Use `copy()` + `delete()` pattern |

### 6.3 Strict Mode Setup

```kotlin
// ✅ Enable StrictMode in debug builds
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .penaltyLog()
            .penaltyFlashScreen() // Red flash on violation
            .build()
    )

    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectLeakedClosableObjects()
            .detectLeakedSqlLiteObjects()
            .detectActivityLeaks()
            .penaltyLog()
            .build()
    )
}
```

---

## 7. Performance Profiling

### 7.1 CPU Profiling (Android Studio)

```
1. Run → Profile 'app'
2. Select CPU panel
3. Choose "Sample Java/Kotlin Methods"
4. Record while performing the action to profile
5. Stop recording → analyze flame graph

Key areas to profile:
- Folder loading (FolderScreen open)
- File list scrolling
- Vault lock/unlock
- Search query execution
- App cold start
```

### 7.2 Memory Profiling

```
1. Run → Profile 'app'
2. Select Memory panel
3. Trigger garbage collection (trash can icon)
4. Capture heap dump
5. Check for:
   - Large bitmap allocations
   - Leaked Activities/Fragments
   - Growing collections (cache not evicting)
   - Duplicate objects

FileHive-specific checks:
- FolderStatsCache size — should be bounded
- Coil memory cache — should respect 15% limit
- File list — should not hold entire filesystem
```

### 7.3 Custom Performance Traces

```kotlin
// ✅ Measure critical operations
object PerfTracer {
    fun <T> trace(label: String, block: () -> T): T {
        if (!BuildConfig.DEBUG) return block()

        val start = System.nanoTime()
        val result = block()
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        Log.d("PerfTrace", "$label: ${String.format("%.2f", elapsed)}ms")
        return result
    }

    suspend fun <T> traceAsync(label: String, block: suspend () -> T): T {
        if (!BuildConfig.DEBUG) return block()

        val start = System.nanoTime()
        val result = block()
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        Log.d("PerfTrace", "$label: ${String.format("%.2f", elapsed)}ms")
        return result
    }
}

// Usage
val files = PerfTracer.traceAsync("loadFolder:$path") {
    scanner.scanDirectory(File(path))
}
```

### 7.4 Systrace / Perfetto

```bash
# Capture system trace (10 seconds)
adb shell perfetto \
  -c - --txt \
  -o /data/misc/perfetto-traces/trace.perfetto-trace \
  <<EOF
buffers: { size_kb: 63488 }
data_sources: {
    config {
        name: "linux.process_stats"
        target_buffer: 0
    }
}
duration_ms: 10000
EOF

# Pull and open in ui.perfetto.dev
adb pull /data/misc/perfetto-traces/trace.perfetto-trace
```

---

## 8. LeakCanary Usage

### 8.1 Setup

```kotlin
// build.gradle.kts — debug only, no code needed
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}
```

### 8.2 Configuration

```kotlin
// Optional: Custom LeakCanary config in Application.onCreate()
if (BuildConfig.DEBUG) {
    LeakCanary.config = LeakCanary.config.copy(
        retainedVisibleThreshold = 3, // Show notification after 3 retained objects
        dumpHeap = true,
        referenceMatchers = LeakCanary.config.referenceMatchers +
            AndroidReferenceMatchers.instanceFieldLeak(
                className = "com.viv3k.filehive.data.storage.FolderStatsCache",
                fieldName = "cache",
                description = "FolderStatsCache holds references to File objects"
            )
    )
}
```

### 8.3 Common Leak Patterns in FileHive

| Leak Source | Why | Fix |
|-------------|-----|-----|
| `FolderStatsCache` (static) | Holds `File` references tied to Activity lifecycle | Use `@Singleton` with Application context |
| Anonymous `BiometricPrompt.Callback` | Holds reference to Activity | Use `WeakReference` or ViewModel |
| `SecurityRepository(context)` | Activity context passed to singleton | Use `@ApplicationContext` |
| Coil `ImageRequest.Builder(context)` | Activity context in long-lived request | Use `LocalContext.current` in Compose |

---

## 9. Debug Build Configuration

```kotlin
// build.gradle.kts — Debug-specific configuration
android {
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // Enable strict mode crash on violations
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## 10. Quick Debug Cheat Sheet

```
┌─────────────────────────────────────────────────────────────────┐
│                    FILEHIVE DEBUG CHEAT SHEET                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  LOGS:     adb logcat -s "FileHive" "*:D"                       │
│  DB:       App Inspection → Database Inspector                  │
│  COMPOSE:  Layout Inspector → Recomposition counts              │
│  MEMORY:   Profiler → Heap dump                                 │
│  ANR:      adb pull /data/anr/traces.txt                        │
│  STORAGE:  adb shell ls -la /storage/emulated/0/.FileHiveBin/   │
│  VAULT:    adb shell ls -la <app_data>/files/.vault/            │
│  PERMS:    adb shell appops get com.viv3k.filehive              │
│  CLEAR:    adb shell pm clear com.viv3k.filehive                │
│  LEAKS:    LeakCanary notification → Analyze                    │
│  PERF:     Profiler → CPU → Sample → Flame graph                │
│                                                                 │
│  VAULT DEBUG:                                                   │
│  - Check EncryptedSharedPrefs: shared_prefs/secure_vault_prefs  │
│  - Check Room: databases/filehive_database                      │
│  - Check vault files: Android/data/.../files/.vault/            │
│                                                                 │
│  CRASH:    adb logcat -b crash                                  │
│  THREADS:  adb shell kill -3 <PID> (dump thread stacks)         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```
