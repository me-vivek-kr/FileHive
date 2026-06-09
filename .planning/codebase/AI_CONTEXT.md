# AI Context

> Coding conventions, AI-specific rules, and architectural patterns for AI-assisted development in FileHive.

---

## 1. Project Identity

```yaml
Project: FileHive
Package: com.viv3k.filehive
Platform: Android (API 31–36)
Language: Kotlin 2.1.0
UI: Jetpack Compose (Material 3)
Architecture: MVVM + Clean Architecture (evolving)
Database: Room 2.6.1
DI: Manual → Hilt (planned migration)
Build: Gradle Kotlin DSL
Type: Offline File Manager + Secure Vault
```

---

## 2. Coding Conventions

### 2.1 Kotlin Style

```kotlin
// ✅ Use data class for models — always immutable
data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val extension: String = "",
    val childCount: Int = 0
)

// ✅ Use sealed interface (not sealed class) for state/result types
sealed interface FolderUiState {
    data object Loading : FolderUiState
    data class Success(val files: List<FileItem>) : FolderUiState
    data class Error(val message: String) : FolderUiState
}

// ✅ Use val (never var) for class properties
// ✅ Use immutable collections (List, not MutableList) in public APIs
// ✅ Use Kotlin Result type or custom sealed class for error handling
// ✅ Extension functions for utility logic, not utility classes
```

### 2.2 Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Package | lowercase, dot-separated | `com.viv3k.filehive.data.repository` |
| Class | PascalCase | `FolderViewModel`, `SecurityRepository` |
| Interface | PascalCase, no "I" prefix | `FileRepository` (not `IFileRepository`) |
| Function | camelCase, verb-first | `loadFolder()`, `deleteFile()` |
| Property | camelCase | `fileList`, `isLoading` |
| Constant | SCREAMING_SNAKE_CASE | `MAX_ATTEMPTS`, `LOCKOUT_DURATION_MS` |
| Composable | PascalCase | `FolderScreen`, `FileRow` |
| StateFlow | `_state` (private), `state` (public) | `_state` / `state` |
| Event function | `on` prefix | `onDeleteFile()`, `onPinDigitEntered()` |
| Boolean property | `is`/`has`/`should` prefix | `isLoading`, `hasPin` |
| File names | Match primary class name | `FolderViewModel.kt` |
| Test files | `ClassNameTest.kt` | `FolderViewModelTest.kt` |
| Module names | lowercase with hyphens | `:core:domain`, `:feature:vault` |

### 2.3 File Organization Rules

```
Feature-based organization:
✅ ui/screens/vault/VaultScreen.kt
✅ ui/screens/vault/VaultViewModel.kt
✅ ui/screens/vault/LockAuthScreen.kt

Layer-based for core:
✅ data/repository/FileRepository.kt
✅ data/database/AppDatabase.kt
✅ core/navigation/AppNavigation.kt

DO NOT:
❌ data/model/FolderViewModel.kt  ← ViewModel in data layer
❌ ui/screens/vault/SecurityRepository.kt  ← Repository in UI layer
```

---

## 3. AI Coding Rules

### 3.1 Must-Follow Rules

```
1. ALWAYS use Kotlin — never Java
2. ALWAYS use Jetpack Compose — never XML layouts
3. ALWAYS use StateFlow — never LiveData
4. ALWAYS use sealed interface — never sealed class (Kotlin 1.9+)
5. ALWAYS use data object — never object for sealed subtypes
6. ALWAYS use collectAsStateWithLifecycle() — never collectAsState()
7. ALWAYS use withContext(Dispatchers.IO) for file/DB operations
8. ALWAYS use viewModelScope for coroutine launches in ViewModels
9. ALWAYS pass events UP and state DOWN in Compose
10. ALWAYS handle error states — never leave a catch block empty
```

### 3.2 Preferred Patterns

```kotlin
// ✅ PREFERRED: UseCase pattern for business logic
class LockFolderUseCase @Inject constructor(
    private val vaultRepository: VaultRepository
) {
    suspend operator fun invoke(path: String): Result<Unit> { ... }
}

// ✅ PREFERRED: Repository interface + implementation
interface FileRepository {
    suspend fun getFiles(path: String): List<FileItem>
}

class FileRepositoryImpl @Inject constructor(
    private val dao: FolderCacheDao
) : FileRepository {
    override suspend fun getFiles(path: String): List<FileItem> { ... }
}

// ✅ PREFERRED: Single event handler in ViewModel
fun onEvent(event: FolderEvent) {
    when (event) {
        is FolderEvent.LoadFolder -> loadFolder(event.path)
        is FolderEvent.DeleteFile -> deleteFile(event.file)
    }
}

// ✅ PREFERRED: Composable preview with sample data
@Preview(showBackground = true)
@Composable
fun FileRowPreview() {
    FileHiveTheme {
        FileRow(
            file = FileItem(name = "photo.jpg", path = "/test", size = 1024L, ...),
            onClick = {},
            onLongClick = {}
        )
    }
}
```

---

## 4. Forbidden Coding Patterns

### 4.1 Absolute Don'ts

```kotlin
// ❌ FORBIDDEN: ViewModel in data layer
package com.viv3k.filehive.data.model
class FolderViewModel : ViewModel() // WRONG PACKAGE

// ❌ FORBIDDEN: Direct DB access in ViewModel
fun lockFolder(context: Context, file: File) {
    val db = AppDatabase.getDatabase(context) // Never do this
    val vaultRepo = VaultRepository(context, db.lockedFolderDao()) // Never do this
}

// ❌ FORBIDDEN: Context in ViewModel (use @ApplicationContext via Hilt)
class MyViewModel(private val context: Context) : ViewModel()

// ❌ FORBIDDEN: GlobalScope for coroutines
GlobalScope.launch { ... }

// ❌ FORBIDDEN: runBlocking on main thread
runBlocking { repository.getFiles() }

// ❌ FORBIDDEN: Mutable state in composable parameters
@Composable
fun FileList(files: MutableList<FileItem>) // Use List<FileItem>

// ❌ FORBIDDEN: Side effects outside LaunchedEffect
@Composable
fun MyScreen(viewModel: MyViewModel) {
    viewModel.loadData() // Called on every recomposition!
}

// ❌ FORBIDDEN: Hardcoded strings in composables
Text("No files found") // Use stringResource(R.string.no_files)

// ❌ FORBIDDEN: suppress warnings without justification
@Suppress("UNCHECKED_CAST") // Why? Document it.

// ❌ FORBIDDEN: Star imports
import com.viv3k.filehive.data.* // Always import specific classes

// ❌ FORBIDDEN: Nested callbacks (callback hell)
repository.getFiles { files ->
    files.forEach { file ->
        repository.getMetadata(file) { metadata ->
            // Use coroutines + Flow instead
        }
    }
}

// ❌ FORBIDDEN: Thread.sleep() or Thread() — use coroutines
Thread.sleep(1000) // Use delay(1000) in coroutine

// ❌ FORBIDDEN: lateinit for ViewModel state
lateinit var state: FolderUiState // Use StateFlow with initial value
```

---

## 5. Compose Conventions

### 5.1 Composable Function Rules

```kotlin
// ✅ Screen composables: take ViewModel + navigation callbacks
@Composable
fun FolderScreen(
    viewModel: FolderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToViewer: (String) -> Unit
)

// ✅ Content composables: stateless, take state + event callbacks
@Composable
private fun FolderContent(
    state: FolderUiState,
    onEvent: (FolderEvent) -> Unit,
    modifier: Modifier = Modifier
)

// ✅ Component composables: reusable, take data + callbacks
@Composable
fun FileRow(
    file: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
)

// ✅ Modifier is always the first optional parameter
// ✅ Callback lambdas are always the last parameters
// ✅ Default Modifier.Companion (not Modifier) for default value
```

### 5.2 State Handling in Compose

```kotlin
// ✅ Collect state with lifecycle awareness
val state by viewModel.state.collectAsStateWithLifecycle()

// ✅ Derived state for computed values
val selectedCount by remember {
    derivedStateOf { state.selectedFiles.size }
}

// ✅ remember for UI-only state
var showDialog by remember { mutableStateOf(false) }
val scrollState = rememberLazyListState()

// ✅ rememberSaveable for state that survives config change
var searchQuery by rememberSaveable { mutableStateOf("") }
```

### 5.3 LazyColumn Best Practices

```kotlin
// ✅ Always provide stable keys
LazyColumn {
    items(
        items = files,
        key = { file -> file.path } // Stable, unique key
    ) { file ->
        FileRow(
            file = file,
            onClick = { onEvent(FolderEvent.OpenFile(file)) }
        )
    }
}

// ✅ Use contentType for heterogeneous lists
LazyColumn {
    items(
        items = files,
        key = { it.path },
        contentType = { if (it.isDirectory) "folder" else "file" }
    ) { file ->
        if (file.isDirectory) FolderItem(file) else FileRow(file)
    }
}
```

---

## 6. Testing Conventions

### 6.1 Test Structure

```
src/
├── main/          ← Production code
├── test/          ← Unit tests (JVM)
│   └── com/viv3k/filehive/
│       ├── viewmodel/
│       │   └── FolderViewModelTest.kt
│       ├── repository/
│       │   └── FileRepositoryTest.kt
│       └── usecase/
│           └── LockFolderUseCaseTest.kt
└── androidTest/   ← Instrumented tests (device)
    └── com/viv3k/filehive/
        ├── ui/
        │   └── FolderScreenTest.kt
        └── database/
            └── FolderCacheDaoTest.kt
```

### 6.2 Test Naming Convention

```kotlin
// Format: `function under test` `scenario` `expected result`
@Test
fun `loadFolder with valid path emits Success state`() { ... }

@Test
fun `verifyPin with incorrect pin increments fail count`() { ... }

@Test
fun `deleteFile permanently removes file from disk`() { ... }
```

### 6.3 Test Patterns

```kotlin
// ✅ Use fakes, not mocks (for repositories)
class FakeFileRepository : FileRepository {
    private val files = mutableListOf<FileItem>()
    var shouldFail = false

    fun setFiles(newFiles: List<FileItem>) { files.clear(); files.addAll(newFiles) }

    override suspend fun getFiles(path: String): List<FileItem> {
        if (shouldFail) throw IOException("Test failure")
        return files.toList()
    }
}

// ✅ Use Turbine for Flow testing
@Test
fun `state emits Loading then Success`() = runTest {
    viewModel.state.test {
        assertEquals(FolderUiState.Idle, awaitItem())
        viewModel.loadFolder("/test")
        assertEquals(FolderUiState.Loading, awaitItem())
        assertIs<FolderUiState.Success>(awaitItem())
    }
}

// ✅ Use MainDispatcherRule for ViewModel tests
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

---

## 7. Repository Rules

### 7.1 Repository Interface Contract

```kotlin
// ✅ Repository interface in domain layer — no Android imports
interface FileRepository {
    suspend fun getFiles(path: String): List<FileItem>
    suspend fun deleteFile(path: String, permanent: Boolean): Result<Unit>
    suspend fun createFile(parentPath: String, name: String): Result<FileItem>
    suspend fun renameFile(path: String, newName: String): Result<FileItem>
    fun observeFolder(path: String): Flow<List<FileItem>>
}

// ✅ Implementation in data layer — Android-specific
class FileRepositoryImpl @Inject constructor(
    private val folderCacheDao: FolderCacheDao,
    private val mediaStoreNotifier: MediaStoreNotifier,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FileRepository {
    // Implementation details...
}
```

### 7.2 Repository Rules

```
1. Repositories are the SINGLE SOURCE OF TRUTH
2. Repository interfaces live in :core:domain
3. Repository implementations live in :core:data
4. Repositories NEVER expose Room entities — map to domain models
5. Repositories NEVER depend on ViewModels
6. Repositories ALWAYS return Result<T> for fallible operations
7. Repositories use Flow for observable data
8. Repositories are injected via Hilt @Singleton
```

---

## 8. Dependency Injection Rules

### 8.1 Current State → Target State

```kotlin
// ❌ CURRENT: Manual instantiation in ViewModel
class LockAuthViewModel(private val securityRepository: SecurityRepository) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Context
                LockAuthViewModel(SecurityRepository(context))
            }
        }
    }
}

// ✅ TARGET: Hilt injection
@HiltViewModel
class LockAuthViewModel @Inject constructor(
    private val verifyPinUseCase: VerifyPinUseCase,
    private val securityRepository: SecurityRepository
) : ViewModel()

// In Composable:
@Composable
fun LockAuthScreen(viewModel: LockAuthViewModel = hiltViewModel()) { ... }
```

### 8.2 DI Rules

```
1. NEVER manually instantiate repositories or use cases
2. ALWAYS use constructor injection (not field injection)
3. Use @Singleton for repositories and databases
4. Use @HiltViewModel for all ViewModels
5. Use @ApplicationContext for Context in injected classes
6. Use @IoDispatcher / @DefaultDispatcher qualifiers for dispatchers
7. Bind interfaces to implementations via @Binds in @Module
8. Provide database/DAO instances via @Provides
```

---

## 9. Error Handling Conventions

```kotlin
// ✅ PREFERRED: Kotlin Result for UseCase return types
suspend fun invoke(path: String): Result<List<FileItem>>

// ✅ PREFERRED: Sealed interface for complex error types
sealed interface FileError {
    data object NotFound : FileError
    data object PermissionDenied : FileError
    data object DiskFull : FileError
    data class Unknown(val cause: Throwable) : FileError
}

// ✅ PREFERRED: Map errors at repository boundary
class FileRepositoryImpl : FileRepository {
    override suspend fun deleteFile(path: String, permanent: Boolean): Result<Unit> {
        return try {
            // file operation
            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(FileError.PermissionDenied)
        } catch (e: IOException) {
            Result.failure(FileError.Unknown(e))
        }
    }
}

// ✅ PREFERRED: Handle errors in ViewModel, expose user-friendly messages
viewModelScope.launch {
    deleteFileUseCase(file.path)
        .onSuccess { _effect.emit(UiEffect.ShowSnackbar("Deleted")) }
        .onFailure { error ->
            val message = when (error) {
                is FileError.PermissionDenied -> "Permission denied"
                is FileError.DiskFull -> "Disk is full"
                else -> "Something went wrong"
            }
            _effect.emit(UiEffect.ShowSnackbar(message))
        }
}
```

---

## 10. Quick Reference Card

```
┌─────────────────────────────────────────────────────────────┐
│                    FileHive AI Quick Ref                     │
├─────────────────────────────────────────────────────────────┤
│  Language:     Kotlin 2.1.0 (JVM 11)                        │
│  Min SDK:      31 (Android 12)                              │
│  Target SDK:   36 (Android 16)                              │
│  UI:           Jetpack Compose + Material 3                 │
│  State:        StateFlow (never LiveData)                   │
│  Async:        Coroutines + Flow                            │
│  DB:           Room 2.6.1 + KSP                             │
│  DI:           Hilt (planned)                               │
│  Nav:          Navigation Compose 2.9.6                     │
│  Images:       Coil 3                                       │
│  Security:     EncryptedSharedPreferences + Keystore        │
│  Build:        Gradle KTS + libs.versions.toml              │
│  Test:         JUnit 4 + Turbine + Compose UI Test          │
│  Package:      com.viv3k.filehive                           │
├─────────────────────────────────────────────────────────────┤
│  DO: sealed interface, data object, Result<T>, Fakes        │
│  DON'T: LiveData, XML, GlobalScope, mutable params          │
└─────────────────────────────────────────────────────────────┘
```
