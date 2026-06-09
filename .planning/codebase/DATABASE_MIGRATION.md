# Database Migration

> Room database migration strategy, schema versioning, testing, rollback handling, and backup for FileHive.

---

## 1. Current Database State

### 1.1 Schema Overview

```kotlin
// AppDatabase.kt — Version 2
@Database(
    entities = [FolderCacheEntity::class, LockedFolderEntity::class],
    version = 2,
    exportSchema = false  // ⚠️ MUST change to true for production migrations
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderCacheDao(): FolderCacheDao
    abstract fun lockedFolderDao(): LockedFolderDao
}
```

### 1.2 Current Tables

**`folder_cache`** — Folder metadata cache
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `path` | TEXT | PRIMARY KEY | Folder absolute path |
| `sizeBytes` | INTEGER | NOT NULL | Cached total size |
| `fileCount` | INTEGER | NOT NULL | Cached file count |
| `lastModified` | INTEGER | NOT NULL | Staleness check timestamp |
| `viewMode` | TEXT | NOT NULL, DEFAULT "List" | User preference (List/Grid) |

**`locked_folders`** — Vault locked folder records
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCERATE | Unique ID |
| `originalPath` | TEXT | NOT NULL | Where folder was before lock |
| `vaultPath` | TEXT | NOT NULL | Current vault location |
| `folderName` | TEXT | NOT NULL | Display name |
| `lockedTime` | INTEGER | NOT NULL | Lock timestamp |
| `itemsCount` | INTEGER | NOT NULL, DEFAULT 0 | Cached item count |

### 1.3 Critical Issues

| Issue | Risk | Fix |
|-------|------|-----|
| `exportSchema = false` | Cannot verify migrations against actual schema | Set to `true` |
| `fallbackToDestructiveMigration()` | **Destroys all user data on schema change** | Remove; use proper migrations |
| No migration from v1→v2 | Unclear what changed | Document retroactively |
| Manual singleton pattern | No DI, thread-safety concerns | Migrate to Hilt |

---

## 2. Migration Strategy

### 2.1 Enable Schema Export

```kotlin
// build.gradle.kts
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// AppDatabase.kt
@Database(
    entities = [FolderCacheEntity::class, LockedFolderEntity::class],
    version = 2,
    exportSchema = true  // ✅ Required for migration testing
)
abstract class AppDatabase : RoomDatabase()
```

**Schema files** will be generated at:
```
app/schemas/
├── com.viv3k.filehive.data.database.AppDatabase/
│   ├── 1.json
│   ├── 2.json
│   └── 3.json  ← Generated when version bumps
```

### 2.2 Writing Migrations

```kotlin
// ✅ Standard migration pattern
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add recycle_bin table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS recycle_bin (
                binPath TEXT NOT NULL PRIMARY KEY,
                originalPath TEXT NOT NULL,
                originalName TEXT NOT NULL,
                deletedAt INTEGER NOT NULL,
                size INTEGER NOT NULL,
                isDirectory INTEGER NOT NULL DEFAULT 0,
                autoDeleteAt INTEGER NOT NULL
            )
        """)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add file_index table for full-text search
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS file_index (
                path TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                extension TEXT NOT NULL,
                parentPath TEXT NOT NULL,
                size INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isDirectory INTEGER NOT NULL DEFAULT 0,
                mimeType TEXT,
                indexedAt INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Create FTS virtual table
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS file_index_fts
            USING fts4(name, extension, parentPath, content=file_index)
        """)

        // Create index on parentPath for folder queries
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_file_index_parent ON file_index(parentPath)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add column to existing table (non-destructive)
        db.execSQL("ALTER TABLE locked_folders ADD COLUMN encryptionKeyAlias TEXT")
        db.execSQL("ALTER TABLE locked_folders ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
    }
}
```

### 2.3 Registering Migrations

```kotlin
// ✅ Register ALL migrations — never skip versions
fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "filehive_database"
    )
        .addMigrations(
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5
        )
        // ✅ Fallback only for development builds
        .apply {
            if (BuildConfig.DEBUG) {
                fallbackToDestructiveMigration()
            }
        }
        .build()
}
```

---

## 3. Schema Versioning Rules

### 3.1 Version Bump Checklist

```
Before bumping database version:

□ 1. Create Migration object: MIGRATION_X_Y
□ 2. Write SQL statements in migrate()
□ 3. Update @Database(version = Y)
□ 4. Update entity classes to match SQL
□ 5. Build → verify schema JSON exported
□ 6. Write MigrationTest
□ 7. Test on device with existing data
□ 8. Add migration to database builder
□ 9. Document changes in CHANGELOG.md
□ 10. Git commit with schema JSON
```

### 3.2 Schema Change Types

| Change Type | Migration Strategy | Risk |
|-------------|-------------------|------|
| Add new table | `CREATE TABLE` | 🟢 Low |
| Add column (nullable) | `ALTER TABLE ADD COLUMN` | 🟢 Low |
| Add column (non-null with default) | `ALTER TABLE ADD COLUMN ... DEFAULT` | 🟢 Low |
| Add index | `CREATE INDEX` | 🟢 Low |
| Remove column | Recreate table (SQLite limitation) | 🔴 High |
| Rename column | Recreate table or API 30+ `ALTER RENAME` | 🟡 Medium |
| Change column type | Recreate table | 🔴 High |
| Remove table | `DROP TABLE` | 🟡 Medium |
| Add NOT NULL to existing column | Recreate table | 🔴 High |

### 3.3 Table Recreation Pattern (Destructive Column Changes)

```kotlin
// ✅ When you need to change column types or remove columns
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Step 1: Create new table with desired schema
        db.execSQL("""
            CREATE TABLE folder_cache_new (
                path TEXT NOT NULL PRIMARY KEY,
                sizeBytes INTEGER NOT NULL,
                fileCount INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                viewMode TEXT NOT NULL DEFAULT 'List',
                lastAccessed INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Step 2: Copy data from old table
        db.execSQL("""
            INSERT INTO folder_cache_new (path, sizeBytes, fileCount, lastModified, viewMode)
            SELECT path, sizeBytes, fileCount, lastModified, viewMode
            FROM folder_cache
        """)

        // Step 3: Drop old table
        db.execSQL("DROP TABLE folder_cache")

        // Step 4: Rename new table
        db.execSQL("ALTER TABLE folder_cache_new RENAME TO folder_cache")
    }
}
```

---

## 4. Migration Testing

### 4.1 Room Migration Test Setup

```kotlin
// build.gradle.kts
dependencies {
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}

// Configure schema location for tests
android {
    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }
}
```

### 4.2 Migration Test Implementation

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(), // Auto-migrations (empty if none)
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate2To3() {
        // Create database at version 2
        val db = helper.createDatabase("test_db", 2).apply {
            // Insert test data at version 2
            execSQL("""
                INSERT INTO locked_folders (id, originalPath, vaultPath, folderName, lockedTime, itemsCount)
                VALUES (1, '/storage/emulated/0/Photos', '/data/.vault/uuid1', 'Photos', 1700000000000, 42)
            """)
            close()
        }

        // Run migration to version 3
        val migratedDb = helper.runMigrationsAndValidate("test_db", 3, true, MIGRATION_2_3)

        // Verify data survived migration
        val cursor = migratedDb.query("SELECT * FROM locked_folders WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("/storage/emulated/0/Photos", cursor.getString(cursor.getColumnIndex("originalPath")))
        assertEquals("Photos", cursor.getString(cursor.getColumnIndex("folderName")))

        // Verify new table exists
        val recycleCursor = migratedDb.query("SELECT count(*) FROM recycle_bin")
        assertTrue(recycleCursor.moveToFirst())
        assertEquals(0, recycleCursor.getInt(0))

        cursor.close()
        recycleCursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate3To4() {
        val db = helper.createDatabase("test_db", 3).apply {
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate("test_db", 4, true, MIGRATION_3_4)

        // Verify FTS table exists
        val cursor = migratedDb.query("SELECT count(*) FROM file_index_fts")
        assertTrue(cursor.moveToFirst())
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrateAllVersions() {
        // Test full migration chain: 2 → 3 → 4 → 5
        val db = helper.createDatabase("test_db", 2).apply {
            // Seed initial data
            execSQL("""
                INSERT INTO folder_cache (path, sizeBytes, fileCount, lastModified, viewMode)
                VALUES ('/storage/emulated/0', 1000000, 500, 1700000000000, 'List')
            """)
            close()
        }

        helper.runMigrationsAndValidate(
            "test_db", 5, true,
            MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5
        ).close()
    }
}
```

---

## 5. Rollback Handling

### 5.1 Rollback Strategy

Room does **not** support native rollback. FileHive must handle this at the application level:

```kotlin
// ✅ Defensive migration with try-catch
val MIGRATION_2_3_SAFE = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS recycle_bin (
                    binPath TEXT NOT NULL PRIMARY KEY,
                    originalPath TEXT NOT NULL,
                    originalName TEXT NOT NULL,
                    deletedAt INTEGER NOT NULL,
                    size INTEGER NOT NULL,
                    isDirectory INTEGER NOT NULL DEFAULT 0,
                    autoDeleteAt INTEGER NOT NULL
                )
            """)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // Log migration failure
            Log.e("Migration", "Migration 2→3 failed", e)
            throw e // Room will handle the failure
        } finally {
            db.endTransaction()
        }
    }
}
```

### 5.2 Fallback Strategy

```kotlin
// ✅ Configurable fallback for release builds
Room.databaseBuilder(context, AppDatabase::class.java, "filehive_database")
    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
    .addCallback(object : RoomDatabase.Callback() {
        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            // Log that destructive migration occurred
            // Re-populate essential data
            Log.w("Database", "Destructive migration triggered — data loss occurred")
        }
    })
    .fallbackToDestructiveMigrationOnDowngrade() // Only for downgrades, not upgrades
    .build()
```

### 5.3 Version Downgrade Protection

```kotlin
// ✅ Handle app version downgrades gracefully
Room.databaseBuilder(context, AppDatabase::class.java, "filehive_database")
    .addMigrations(*ALL_MIGRATIONS.toTypedArray())
    .fallbackToDestructiveMigrationOnDowngrade() // Safe: only on downgrade
    // Never: .fallbackToDestructiveMigration()  ← Destroys data on upgrade too
    .build()
```

---

## 6. Database Backup Considerations

### 6.1 Backup Strategy

```kotlin
class DatabaseBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dbFile: File
        get() = context.getDatabasePath("filehive_database")

    private val backupDir: File
        get() = File(context.filesDir, "db_backups").apply { mkdirs() }

    // ✅ Create backup before migration
    suspend fun createBackup(tag: String = ""): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupName = "filehive_backup_${tag}_$timestamp.db"
        val backupFile = File(backupDir, backupName)

        // Close database before backup
        AppDatabase.closeDatabase()

        dbFile.copyTo(backupFile, overwrite = true)

        // Also backup WAL and SHM files
        File("${dbFile.path}-wal").let { wal ->
            if (wal.exists()) wal.copyTo(File("${backupFile.path}-wal"), overwrite = true)
        }
        File("${dbFile.path}-shm").let { shm ->
            if (shm.exists()) shm.copyTo(File("${backupFile.path}-shm"), overwrite = true)
        }

        // Prune old backups (keep last 5)
        pruneOldBackups(keep = 5)

        backupFile
    }

    // ✅ Restore from backup
    suspend fun restoreBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        if (!backupFile.exists()) return@withContext false

        AppDatabase.closeDatabase()

        return@withContext try {
            backupFile.copyTo(dbFile, overwrite = true)

            File("${backupFile.path}-wal").let { wal ->
                if (wal.exists()) wal.copyTo(File("${dbFile.path}-wal"), overwrite = true)
            }
            File("${backupFile.path}-shm").let { shm ->
                if (shm.exists()) shm.copyTo(File("${dbFile.path}-shm"), overwrite = true)
            }

            true
        } catch (e: Exception) {
            Log.e("Backup", "Restore failed", e)
            false
        }
    }

    // ✅ List available backups
    fun listBackups(): List<BackupInfo> {
        return backupDir.listFiles()
            ?.filter { it.name.endsWith(".db") }
            ?.map { BackupInfo(it.name, it.length(), it.lastModified(), it) }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    private fun pruneOldBackups(keep: Int) {
        backupDir.listFiles()
            ?.filter { it.name.endsWith(".db") }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(keep)
            ?.forEach { it.delete() }
    }
}

data class BackupInfo(
    val name: String,
    val size: Long,
    val timestamp: Long,
    val file: File
)
```

### 6.2 Pre-Migration Backup Hook

```kotlin
// ✅ Automatically backup before any migration runs
class SafeMigration(
    startVersion: Int,
    endVersion: Int,
    private val backupManager: DatabaseBackupManager,
    private val migrationBlock: (SupportSQLiteDatabase) -> Unit
) : Migration(startVersion, endVersion) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // Backup happens at app level before Room opens
        migrationBlock(db)
    }
}

// Usage in Application.onCreate()
lifecycleScope.launch(Dispatchers.IO) {
    val currentVersion = getDatabaseVersion()
    val targetVersion = AppDatabase.TARGET_VERSION

    if (currentVersion < targetVersion) {
        backupManager.createBackup("pre_migration_${currentVersion}_to_$targetVersion")
    }
}
```

---

## 7. Future Schema Roadmap

| Version | Changes | Entities Added/Modified |
|---------|---------|------------------------|
| v2 (current) | Added `locked_folders` table | `LockedFolderEntity` |
| v3 (planned) | Add `recycle_bin` table | `RecycleBinEntity` |
| v4 (planned) | Add `file_index` + FTS | `FileIndexEntity`, `FileIndexFts` |
| v5 (planned) | Add encryption columns to `locked_folders` | Modify `LockedFolderEntity` |
| v6 (planned) | Add `user_preferences` table (replace DataStore) | `UserPreferencesEntity` |
| v7 (planned) | Add `recent_files` table | `RecentFileEntity` |

---

## 8. Database Maintenance

### 8.1 Periodic Cleanup Worker

```kotlin
class DatabaseMaintenanceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)

        // Purge expired recycle bin entries
        db.recycleBinDao().purgeExpired()

        // Clean stale file index entries
        db.fileIndexDao().deleteStaleEntries(
            threshold = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days
        )

        // Vacuum database to reclaim space
        db.openHelper.writableDatabase.execSQL("VACUUM")

        // Analyze for query optimizer
        db.openHelper.writableDatabase.execSQL("ANALYZE")

        return Result.success()
    }
}

// Schedule weekly maintenance
fun scheduleMaintenanceWorker(context: Context) {
    val request = PeriodicWorkRequestBuilder<DatabaseMaintenanceWorker>(
        repeatInterval = 7, repeatIntervalTimeUnit = TimeUnit.DAYS
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "db_maintenance",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
```
