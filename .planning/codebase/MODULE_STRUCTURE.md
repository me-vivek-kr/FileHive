# Module Structure

> Production-grade modularization roadmap for FileHive — from monolithic `:app` to scalable multi-module architecture.

---

## 1. Current State Analysis

FileHive currently ships as a **single `:app` module** with a hybrid feature/layer package structure:

```
app/src/main/java/com/viv3k/filehive/
├── core/              ← App-wide utilities (navigation, Coil)
├── data/              ← Data layer (Room, repositories, storage, models)
│   ├── common/
│   ├── database/      ← AppDatabase, DAOs, Entities
│   ├── model/         ← FolderModel, FolderViewModel (⚠️ wrong layer)
│   ├── repository/    ← FileRepository, VaultRepository, SecurityRepository
│   └── storage/       ← FolderStatsCache, storage volume helpers
├── ui/                ← Presentation layer
│   ├── components/    ← Reusable composables
│   ├── screens/       ← Feature screens (folder, vault, search, etc.)
│   ├── theme/         ← Material 3 theme
│   └── utils/
└── MainActivity.kt
```

### Critical Issues
| Issue | Impact | Priority |
|-------|--------|----------|
| `FolderViewModel` lives in `data/model/` | Violates layer separation; circular dependency risk | 🔴 High |
| No `domain` layer exists | Business logic leaks into ViewModels and repositories | 🔴 High |
| No Hilt/DI framework | Manual instantiation scattered across ViewModels | 🟡 Medium |
| Single module | Slow build times as project grows; no compile-time isolation | 🟡 Medium |
| `SecurityRepository` directly in `data/repository/` with vault-specific UI in `ui/screens/vault/` | Cross-cutting concern; needs security-specific module | 🟡 Medium |

---

## 2. Target Module Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                              :app                                    │
│  (Application class, MainActivity, Hilt entry point, Navigation)     │
└─────────┬───────────────┬───────────────┬────────────────────────────┘
          │               │               │
          ▼               ▼               ▼
┌─────────────┐  ┌──────────────┐  ┌──────────────┐
│  :feature:  │  │  :feature:   │  │  :feature:   │
│   browser   │  │    vault     │  │   search     │  ...more features
└──────┬──────┘  └──────┬───────┘  └──────┬───────┘
       │                │                 │
       ▼                ▼                 ▼
┌──────────────────────────────────────────────────┐
│                  :core:domain                     │
│  (UseCases, Domain Models, Repository Interfaces) │
└──────────────────────┬───────────────────────────┘
                       │
       ┌───────────────┼───────────────┐
       ▼               ▼               ▼
┌────────────┐  ┌────────────┐  ┌─────────────┐
│ :core:data │  │:core:      │  │:core:       │
│ (Room, DAO │  │ security   │  │ storage     │
│  Repos)    │  │(Keystore,  │  │(MediaStore, │
│            │  │ Crypto)    │  │ SAF, IO)    │
└────────────┘  └────────────┘  └─────────────┘
       │               │               │
       ▼               ▼               ▼
┌──────────────────────────────────────────────────┐
│               :core:common                        │
│  (Extensions, Constants, Result wrappers, DI)     │
└──────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────┐
│               :core:ui                            │
│  (Design system, Theme, Reusable composables)     │
└──────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────┐
│             :core:testing                         │
│  (Test fixtures, fakes, shared test utilities)     │
└──────────────────────────────────────────────────┘
```

---

## 3. Module Definitions

### 3.1 `:app` — Application Shell

**Responsibility**: Wire everything together. Own the `Application` class, `MainActivity`, global navigation graph, and Hilt `@HiltAndroidApp` entry point.

```
:app/
├── FileHiveApplication.kt        ← @HiltAndroidApp
├── MainActivity.kt               ← @AndroidEntryPoint
├── navigation/
│   └── AppNavigation.kt          ← Top-level NavHost
└── di/
    └── AppModule.kt              ← App-scoped bindings
```

**Dependencies**: All `:feature:*` modules, `:core:domain`, `:core:ui`

**Rules**:
- ✅ May depend on any module
- ❌ No business logic
- ❌ No direct data access
- ❌ No other module may depend on `:app`

---

### 3.2 `:feature:browser` — File Browsing

**Responsibility**: Folder listing, file grid/row views, file operations (create, delete, rename), breadcrumb navigation.

```
:feature:browser/
├── ui/
│   ├── FolderScreen.kt
│   ├── FolderTopBar.kt
│   ├── FileGrid.kt
│   ├── FileRow.kt
│   └── NewFileFolderDialog.kt
├── viewmodel/
│   └── FolderViewModel.kt          ← Moved FROM data/model/
└── navigation/
    └── BrowserNavigation.kt
```

**Dependencies**: `:core:domain`, `:core:ui`, `:core:common`

---

### 3.3 `:feature:vault` — Secure Vault

**Responsibility**: PIN setup/verification, biometrics, security questions, locked folder management, vault browsing.

```
:feature:vault/
├── ui/
│   ├── LockScreen.kt
│   ├── VaultScreen.kt
│   └── components/
│       ├── PinPad.kt
│       └── SecurityQuestionDialog.kt
├── viewmodel/
│   ├── LockAuthViewModel.kt
│   └── VaultViewModel.kt
└── navigation/
    └── VaultNavigation.kt
```

**Dependencies**: `:core:domain`, `:core:security`, `:core:ui`, `:core:common`

---

### 3.4 `:feature:search` — File Search

**Responsibility**: Full-text search across file system, filter chips, search result rendering.

```
:feature:search/
├── ui/
│   ├── SearchScreen.kt
│   ├── SearchBar.kt
│   ├── SearchResultItem.kt
│   └── FilterChip.kt
├── viewmodel/
│   └── SearchViewModel.kt
└── navigation/
    └── SearchNavigation.kt
```

**Dependencies**: `:core:domain`, `:core:ui`, `:core:common`

---

### 3.5 `:feature:viewer` — Media Viewer

**Responsibility**: Image/video viewer, zoom, share actions.

**Dependencies**: `:core:domain`, `:core:ui`, `:core:common`

---

### 3.6 `:feature:recyclebin` — Recycle Bin

**Responsibility**: Deleted file recovery, permanent deletion, bin management.

**Dependencies**: `:core:domain`, `:core:ui`, `:core:common`

---

### 3.7 `:feature:home` — Home Screen

**Responsibility**: Storage overview, quick access grid, navigation entry points.

**Dependencies**: `:core:domain`, `:core:ui`, `:core:common`

---

### 3.8 `:core:domain` — Domain Layer

**Responsibility**: Business logic via UseCases, domain models (pure Kotlin), and repository interfaces.

```
:core:domain/
├── model/
│   ├── FileItem.kt
│   ├── FolderStats.kt
│   ├── LockedFolder.kt
│   └── StorageVolume.kt
├── repository/
│   ├── FileRepository.kt           ← Interface only
│   ├── VaultRepository.kt          ← Interface only
│   └── SecurityRepository.kt       ← Interface only
└── usecase/
    ├── file/
    │   ├── GetFolderContentsUseCase.kt
    │   ├── DeleteFileUseCase.kt
    │   ├── CreateFileUseCase.kt
    │   └── MoveFileUseCase.kt
    ├── vault/
    │   ├── LockFolderUseCase.kt
    │   ├── UnlockFolderUseCase.kt
    │   └── VerifyPinUseCase.kt
    ├── search/
    │   └── SearchFilesUseCase.kt
    └── storage/
        ├── GetStorageStatsUseCase.kt
        └── GetFolderStatsUseCase.kt
```

**Dependencies**: `:core:common` only

**Rules**:
- ✅ Pure Kotlin — no Android framework imports
- ✅ Repository interfaces only (implementations in `:core:data`)
- ❌ No Room, no Context, no Android SDK references
- ❌ No UI or presentation concerns

**UseCase Pattern**:

```kotlin
class LockFolderUseCase(
    private val vaultRepository: VaultRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(folderPath: String): Result<Unit> {
        val folder = fileRepository.getFile(folderPath)
            ?: return Result.failure(FileNotFoundException("Folder not found"))

        if (!folder.isDirectory) {
            return Result.failure(IllegalArgumentException("Not a directory"))
        }

        return vaultRepository.lockFolder(folder)
    }
}
```

---

### 3.9 `:core:data` — Data Layer

**Responsibility**: Repository implementations, Room database, DAOs, entities, data mappers.

```
:core:data/
├── database/
│   ├── AppDatabase.kt
│   ├── dao/
│   │   ├── FolderCacheDao.kt
│   │   └── LockedFolderDao.kt
│   ├── entity/
│   │   ├── FolderCacheEntity.kt
│   │   └── LockedFolderEntity.kt
│   └── mapper/
│       ├── FolderCacheMapper.kt
│       └── LockedFolderMapper.kt
├── repository/
│   ├── FileRepositoryImpl.kt
│   ├── VaultRepositoryImpl.kt
│   └── SecurityRepositoryImpl.kt
├── cache/
│   └── FolderStatsCache.kt
└── di/
    └── DataModule.kt               ← @Module @InstallIn(SingletonComponent)
```

**Dependencies**: `:core:domain`, `:core:common`, `:core:storage`, `:core:security`

---

### 3.10 `:core:security` — Security Infrastructure

**Responsibility**: Android Keystore, EncryptedSharedPreferences, biometric prompt manager, crypto utilities.

```
:core:security/
├── crypto/
│   ├── KeystoreManager.kt
│   ├── CryptoEngine.kt
│   └── HashUtils.kt
├── biometric/
│   └── BiometricAuthManager.kt
├── preferences/
│   └── SecurePreferencesManager.kt
└── di/
    └── SecurityModule.kt
```

**Dependencies**: `:core:common`

---

### 3.11 `:core:storage` — Storage Abstraction

**Responsibility**: MediaStore queries, SAF wrappers, file system IO, scoped storage compatibility.

```
:core:storage/
├── mediastore/
│   └── MediaStoreDataSource.kt
├── saf/
│   └── SafDocumentProvider.kt
├── filesystem/
│   ├── FileSystemScanner.kt
│   └── StorageVolumeProvider.kt
├── stats/
│   ├── StorageStats.kt
│   └── computeFolderStats.kt
└── di/
    └── StorageModule.kt
```

**Dependencies**: `:core:common`

---

### 3.12 `:core:ui` — Design System

**Responsibility**: Theme, colors, typography, shared composables (thumbnails, icons, shimmer, chips).

```
:core:ui/
├── theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
├── components/
│   ├── BreadcrumbNavigation.kt
│   ├── CircularProgressArc.kt
│   ├── DotsTypingIndicator.kt
│   ├── FileIcons.kt
│   ├── FileThumbnail.kt
│   ├── FolderItem.kt
│   ├── LoadingListShimmer.kt
│   └── StorageChip.kt
└── extensions/
    └── ModifierExtensions.kt
```

**Dependencies**: `:core:common`

---

### 3.13 `:core:common` — Shared Utilities

**Responsibility**: Extension functions, result wrappers, constants, dispatchers, logging.

```
:core:common/
├── result/
│   └── AppResult.kt
├── extensions/
│   ├── FileExtensions.kt
│   ├── FlowExtensions.kt
│   └── StringExtensions.kt
├── constants/
│   └── FileConstants.kt
├── dispatcher/
│   └── AppDispatchers.kt
└── di/
    └── CommonModule.kt
```

**Dependencies**: None (leaf module)

---

### 3.14 `:core:testing` — Test Infrastructure

**Responsibility**: Shared test utilities, fakes, test fixtures, coroutine test rules.

```
:core:testing/
├── fake/
│   ├── FakeFileRepository.kt
│   ├── FakeVaultRepository.kt
│   └── FakeSecurityRepository.kt
├── rule/
│   └── MainDispatcherRule.kt
└── fixture/
    └── TestFileFactory.kt
```

**Dependencies**: `:core:domain`, JUnit, Turbine, Coroutines Test

---

## 4. Dependency Rules

### Module Dependency Matrix

| Module | May Depend On | Must NOT Depend On |
|--------|--------------|-------------------|
| `:app` | Everything | — |
| `:feature:*` | `:core:domain`, `:core:ui`, `:core:common` | Other `:feature:*` modules, `:core:data` |
| `:core:domain` | `:core:common` | `:core:data`, `:core:security`, Android SDK |
| `:core:data` | `:core:domain`, `:core:common`, `:core:storage`, `:core:security` | `:feature:*`, `:app` |
| `:core:security` | `:core:common` | `:core:data`, `:feature:*` |
| `:core:storage` | `:core:common` | `:core:data`, `:feature:*` |
| `:core:ui` | `:core:common` | `:core:data`, `:core:domain` |
| `:core:common` | Nothing | Everything |
| `:core:testing` | `:core:domain`, `:core:common` | `:core:data` (use fakes) |

### Enforced Rules

```kotlin
// settings.gradle.kts — Full module registration
rootProject.name = "FileHive"

include(":app")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:security")
include(":core:storage")
include(":core:ui")
include(":core:testing")
include(":feature:browser")
include(":feature:vault")
include(":feature:search")
include(":feature:viewer")
include(":feature:recyclebin")
include(":feature:home")
```

### Convention Plugin (build-logic/)

```kotlin
// build-logic/convention/src/main/kotlin/filehive.android.library.gradle.kts
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 36
    defaultConfig { minSdk = 31 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

---

## 5. Hilt Dependency Injection Setup

### 5.1 Application Entry Point

```kotlin
// :app
@HiltAndroidApp
class FileHiveApplication : Application()
```

### 5.2 Data Module (`:core:data`)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "filehive_database"
            )
                .addMigrations(MIGRATION_2_3)
                .build()
        }

        @Provides
        fun provideFolderCacheDao(db: AppDatabase): FolderCacheDao = db.folderCacheDao()

        @Provides
        fun provideLockedFolderDao(db: AppDatabase): LockedFolderDao = db.lockedFolderDao()
    }
}
```

### 5.3 ViewModel Injection (`:feature:vault`)

```kotlin
// Before (current manual factory)
companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Context
            LockAuthViewModel(SecurityRepository(context))
        }
    }
}

// After (Hilt injection)
@HiltViewModel
class LockAuthViewModel @Inject constructor(
    private val verifyPinUseCase: VerifyPinUseCase,
    private val setupPinUseCase: SetupPinUseCase,
    private val securityRepository: SecurityRepository
) : ViewModel()
```

---

## 6. Migration Strategy

### Phase 1: Introduce Domain Layer (In-Module)

**Timeline**: ~2 days | **Risk**: Low

1. Create `domain/` package inside existing `:app` module
2. Extract repository interfaces from concrete classes
3. Create UseCase classes
4. Update ViewModels to depend on interfaces
5. Move `FolderViewModel` from `data/model/` → `ui/screens/folder/`

### Phase 2: Introduce Hilt

**Timeline**: ~1 day | **Risk**: Low

1. Add Hilt dependencies to `build.gradle.kts`
2. Create `FileHiveApplication` with `@HiltAndroidApp`
3. Annotate `MainActivity` with `@AndroidEntryPoint`
4. Create `@Module` classes for existing repositories
5. Replace manual ViewModel factories with `@HiltViewModel`

### Phase 3: Extract Core Modules

**Timeline**: ~3 days | **Risk**: Medium

1. `:core:common` — Extract extension functions, constants, result wrappers
2. `:core:ui` — Extract theme, shared composables
3. `:core:domain` — Move domain models, interfaces, use cases
4. `:core:data` — Move Room, DAOs, repository implementations
5. `:core:security` — Extract SecurityRepository internals, Keystore, crypto
6. `:core:storage` — Extract file system, MediaStore wrappers

### Phase 4: Extract Feature Modules

**Timeline**: ~3 days | **Risk**: Medium

1. `:feature:browser` — FolderScreen and related components
2. `:feature:vault` — LockScreen, VaultScreen, related ViewModels
3. `:feature:search` — SearchScreen, SearchViewModel
4. `:feature:viewer` — ImageViewerScreen
5. `:feature:recyclebin` — RecycleBinScreen
6. `:feature:home` — HomeScreen

### Phase 5: Convention Plugins

**Timeline**: ~1 day | **Risk**: Low

1. Create `build-logic/` with convention plugins
2. Standardize build configuration across all modules

---

## 7. Future Module Roadmap

| Module | Purpose | Priority |
|--------|---------|----------|
| `:feature:settings` | App preferences, theme toggle, security settings | 🟢 After vault stabilization |
| `:feature:cloud` | Cloud backup integration (Google Drive, etc.) | 🔵 Future |
| `:core:network` | Retrofit client, API models for cloud sync | 🔵 Future |
| `:core:analytics` | Event tracking, crash reporting | 🟡 Pre-launch |
| `:core:notification` | Background operation notifications | 🟡 Pre-launch |
| `:benchmark` | Macro/micro benchmarks for startup and scrolling | 🔵 Future |

---

## 8. Build Performance Impact

| Metric | Single Module (Current) | Multi-Module (Target) |
|--------|------------------------|----------------------|
| Clean Build | ~45s | ~60s (first time only) |
| Incremental Build (1 file) | ~20s | ~5-8s |
| Parallel Build | Not possible | Up to 4x speedup |
| KSP Processing | Full reprocess | Module-scoped |
| CI Cache Hit Rate | ~30% | ~80% |

> **Key insight**: Multi-module architecture pays for itself after the first week of active development through dramatically faster incremental builds.
