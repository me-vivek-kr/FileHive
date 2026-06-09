# State Management

> Scalable Compose state architecture for FileHive — UiState, UiEvent, UiEffect, and Flow-driven reactive pipelines.

---

## 1. Architecture Overview

FileHive adopts the **MVI-inspired** (Model-View-Intent) state pattern with three distinct channels:

```
┌────────────┐     UiEvent      ┌──────────────┐      UiState       ┌────────────┐
│            │ ───────────────▶  │              │  ───────────────▶   │            │
│   Compose  │                  │  ViewModel   │                     │   Compose  │
│     UI     │  ◀───────────────│              │                     │     UI     │
│            │     UiEffect     │              │                     │            │
└────────────┘   (one-shot)     └──────┬───────┘                     └────────────┘
                                       │
                                       ▼
                                ┌──────────────┐
                                │   UseCase /  │
                                │  Repository  │
                                └──────────────┘
```

| Channel | Type | Purpose | Example |
|---------|------|---------|---------|
| **UiState** | `StateFlow<T>` | Persistent screen state that survives recomposition | File list, loading indicator, error message |
| **UiEvent** | Function calls | User intentions (one-to-many state transitions) | `onDeleteFile()`, `onPinDigitEntered()` |
| **UiEffect** | `SharedFlow<T>` | One-shot side effects, consumed exactly once | Show toast, navigate, show snackbar |

---

## 2. UiState Pattern

### 2.1 Sealed Interface for Screen State

Every screen must define a sealed interface representing all possible states:

```kotlin
// ✅ Recommended: Sealed interface with data objects
sealed interface FolderUiState {
    data object Idle : FolderUiState
    data object Loading : FolderUiState
    data class Success(
        val files: List<FileItem>,
        val fileCount: Int,
        val totalSize: Long,
        val latestModified: Long,
        val viewMode: ViewMode = ViewMode.LIST,
        val sortOrder: SortOrder = SortOrder.NAME_ASC
    ) : FolderUiState
    data class Error(
        val message: String,
        val retryAction: (() -> Unit)? = null
    ) : FolderUiState
}

enum class ViewMode { LIST, GRID }
enum class SortOrder { NAME_ASC, NAME_DESC, SIZE_ASC, SIZE_DESC, DATE_ASC, DATE_DESC }
```

### 2.2 Current Implementation (Technical Debt)

```kotlin
// ⚠️ Current: Uses sealed class + object (pre-Kotlin 1.9 idiom)
sealed class FolderUiState {
    object Idle : FolderUiState()
    object Loading : FolderUiState()
    data class Loaded(...) : FolderUiState()
    data class Error(val message: String) : FolderUiState()
}
```

**Migration**: Replace `sealed class` with `sealed interface` and `object` with `data object` across all state definitions.

### 2.3 Complex Screen State with Multiple Concerns

For screens with multiple independent state dimensions, use a composite data class:

```kotlin
// ✅ For VaultScreen — multiple concerns in one state
data class VaultScreenState(
    val authState: AuthState = AuthState.Checking,
    val lockedFolders: List<LockedFolderUi> = emptyList(),
    val isRefreshing: Boolean = false,
    val selectedFolders: Set<String> = emptySet(),
    val dialogState: VaultDialogState = VaultDialogState.None
)

sealed interface AuthState {
    data object Checking : AuthState
    data object SetupPin : AuthState
    data object ConfirmPin : AuthState
    data object SetupSecurityQuestion : AuthState
    data object Unlock : AuthState
    data object ForgotPassword : AuthState
    data object Unlocked : AuthState
}

sealed interface VaultDialogState {
    data object None : VaultDialogState
    data class ConfirmUnlock(val folder: LockedFolderUi) : VaultDialogState
    data class Error(val message: String) : VaultDialogState
}
```

### 2.4 State Update Rules

```kotlin
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val getFolderContents: GetFolderContentsUseCase,
    private val deleteFile: DeleteFileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<FolderUiState>(FolderUiState.Idle)
    val state: StateFlow<FolderUiState> = _state.asStateFlow()

    // ✅ Always update state on the main thread via StateFlow
    // ✅ Use copy() for data class states to avoid full replacement
    // ✅ Emit Loading before async operations

    fun loadFolder(path: String) {
        viewModelScope.launch {
            _state.value = FolderUiState.Loading
            getFolderContents(path)
                .onSuccess { files ->
                    _state.value = FolderUiState.Success(
                        files = files,
                        fileCount = files.size,
                        totalSize = files.sumOf { it.size },
                        latestModified = files.maxOfOrNull { it.lastModified } ?: 0L
                    )
                }
                .onFailure { error ->
                    _state.value = FolderUiState.Error(
                        message = error.message ?: "Failed to load folder",
                        retryAction = { loadFolder(path) }
                    )
                }
        }
    }

    // ✅ Optimistic updates for responsive UI
    fun onDeleteFile(file: FileItem) {
        val currentState = _state.value
        if (currentState !is FolderUiState.Success) return

        // Optimistic: remove from list immediately
        _state.value = currentState.copy(
            files = currentState.files.filter { it.path != file.path },
            fileCount = currentState.fileCount - 1
        )

        viewModelScope.launch {
            deleteFile(file.path, isPermanent = false)
                .onFailure {
                    // Rollback: restore original state
                    _state.value = currentState
                    _effect.emit(UiEffect.ShowSnackbar("Delete failed"))
                }
                .onSuccess {
                    _effect.emit(UiEffect.ShowSnackbar("Moved to recycle bin"))
                }
        }
    }
}
```

---

## 3. UiEvent Pattern

### 3.1 Event Definition

```kotlin
// ✅ Sealed interface for all user actions on a screen
sealed interface FolderEvent {
    data class LoadFolder(val path: String) : FolderEvent
    data class DeleteFile(val file: FileItem) : FolderEvent
    data class RenameFile(val file: FileItem, val newName: String) : FolderEvent
    data class ToggleViewMode(val mode: ViewMode) : FolderEvent
    data class ChangeSortOrder(val order: SortOrder) : FolderEvent
    data class SelectFile(val file: FileItem) : FolderEvent
    data object ClearSelection : FolderEvent
    data object Refresh : FolderEvent
}
```

### 3.2 Event Processing in ViewModel

```kotlin
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val getFolderContents: GetFolderContentsUseCase
) : ViewModel() {

    // Central event handler
    fun onEvent(event: FolderEvent) {
        when (event) {
            is FolderEvent.LoadFolder -> loadFolder(event.path)
            is FolderEvent.DeleteFile -> onDeleteFile(event.file)
            is FolderEvent.ToggleViewMode -> toggleViewMode(event.mode)
            is FolderEvent.ChangeSortOrder -> changeSortOrder(event.order)
            is FolderEvent.SelectFile -> toggleSelection(event.file)
            is FolderEvent.ClearSelection -> clearSelection()
            is FolderEvent.Refresh -> refresh()
            is FolderEvent.RenameFile -> renameFile(event.file, event.newName)
        }
    }
}
```

### 3.3 Composable Integration

```kotlin
@Composable
fun FolderScreen(
    viewModel: FolderViewModel = hiltViewModel(),
    onNavigateToViewer: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FolderContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToViewer = onNavigateToViewer
    )
}

// ✅ Stateless composable — receives state + event callback
@Composable
private fun FolderContent(
    state: FolderUiState,
    onEvent: (FolderEvent) -> Unit,
    onNavigateToViewer: (String) -> Unit
) {
    when (state) {
        is FolderUiState.Idle -> { /* Initial state */ }
        is FolderUiState.Loading -> LoadingListShimmer()
        is FolderUiState.Success -> {
            FileList(
                files = state.files,
                viewMode = state.viewMode,
                onFileClick = { file ->
                    if (file.isDirectory) {
                        onEvent(FolderEvent.LoadFolder(file.path))
                    } else {
                        onNavigateToViewer(file.path)
                    }
                },
                onDeleteClick = { file -> onEvent(FolderEvent.DeleteFile(file)) },
                onToggleViewMode = { mode -> onEvent(FolderEvent.ToggleViewMode(mode)) }
            )
        }
        is FolderUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = state.retryAction
            )
        }
    }
}
```

---

## 4. UiEffect Pattern (One-Shot Side Effects)

### 4.1 Effect Definition

```kotlin
sealed interface UiEffect {
    data class ShowSnackbar(val message: String) : UiEffect
    data class Navigate(val route: String) : UiEffect
    data class ShowToast(val message: String) : UiEffect
    data object NavigateBack : UiEffect
    data class LaunchBiometricPrompt(val title: String) : UiEffect
}
```

### 4.2 Effect Emission in ViewModel

```kotlin
@HiltViewModel
class VaultViewModel @Inject constructor(...) : ViewModel() {

    // ✅ SharedFlow for one-shot events — replay = 0 ensures single consumption
    private val _effect = MutableSharedFlow<UiEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<UiEffect> = _effect.asSharedFlow()

    fun onUnlockFolder(folder: LockedFolderUi) {
        viewModelScope.launch {
            unlockFolderUseCase(folder.id)
                .onSuccess {
                    _effect.emit(UiEffect.ShowSnackbar("Folder unlocked successfully"))
                }
                .onFailure {
                    _effect.emit(UiEffect.ShowSnackbar("Failed to unlock folder"))
                }
        }
    }
}
```

### 4.3 Effect Collection in Composable

```kotlin
@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Collect effects in LaunchedEffect — lifecycle-aware
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is UiEffect.NavigateBack -> onNavigateBack()
                is UiEffect.ShowToast -> { /* Show toast */ }
                else -> {}
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        VaultContent(state = state, onEvent = viewModel::onEvent)
    }
}
```

---

## 5. Compose State Hoisting Rules

### 5.1 Three-Tier Hoisting Strategy

```
┌─────────────────────────────────────────┐
│  Tier 1: ViewModel State                │  StateFlow<UiState>
│  ─ Survives configuration changes       │  Persists across recomposition
│  ─ Business/domain state                │  Examples: file list, auth state
├─────────────────────────────────────────┤
│  Tier 2: Screen-Level remember()        │  Scaffold, dialog, bottom sheet
│  ─ UI-only transient state              │  Examples: scroll position,
│  ─ Lost on configuration change         │  expanded/collapsed states
├─────────────────────────────────────────┤
│  Tier 3: Component-Level remember()     │  Individual composable internal state
│  ─ Isolated, self-contained state       │  Examples: animation state,
│  ─ Never leaks to parent                │  hover state, ripple
└─────────────────────────────────────────┘
```

### 5.2 Rules

```kotlin
// ✅ CORRECT: State hoisted to screen level, stateless child
@Composable
fun FolderScreen(viewModel: FolderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FolderContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
private fun FolderContent(state: FolderUiState, onEvent: (FolderEvent) -> Unit) {
    // Stateless — receives everything it needs as parameters
}

// ❌ WRONG: Child composable collecting from ViewModel directly
@Composable
fun FileRow(viewModel: FolderViewModel = hiltViewModel()) {
    // This creates a new ViewModel scoped to the wrong lifecycle!
}

// ❌ WRONG: Business state in remember()
@Composable
fun FolderScreen() {
    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    // Business state MUST live in ViewModel, not Compose remember
}

// ✅ CORRECT: UI-only state in remember()
@Composable
fun FolderScreen() {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    // Pure UI state — OK in remember()
}
```

### 5.3 FileHive-Specific Hoisting Map

| State | Tier | Holder | Rationale |
|-------|------|--------|-----------|
| File list | Tier 1 | `FolderViewModel` | Survives config change, expensive to recompute |
| PIN input digits | Tier 1 | `LockAuthViewModel` | Security-sensitive, needs business validation |
| Scroll position in file list | Tier 2 | `rememberLazyListState()` | UI-only, cheap to recreate |
| Delete confirmation dialog | Tier 2 | `remember { mutableStateOf(false) }` | Transient UI state |
| Search query text | Tier 1 | `SearchViewModel` | Triggers async operations |
| Bottom sheet expansion | Tier 2 | `rememberModalBottomSheetState()` | UI animation state |
| File thumbnail animation | Tier 3 | Internal `Animatable` | Component-isolated |

---

## 6. Flow Best Practices

### 6.1 Repository Flow Patterns

```kotlin
// ✅ Cold Flow for one-shot data loads
class FileRepositoryImpl @Inject constructor(
    private val folderCacheDao: FolderCacheDao
) : FileRepository {

    override fun getFolderContents(path: String): Flow<List<FileItem>> = flow {
        val folder = File(path)
        val files = withContext(Dispatchers.IO) {
            folder.listFiles()?.map { it.toFileItem() } ?: emptyList()
        }
        emit(files)
    }.flowOn(Dispatchers.IO)
}

// ✅ Room DAO returns Flow — auto-updates on data change
@Dao
interface LockedFolderDao {
    @Query("SELECT * FROM locked_folders ORDER BY lockedTime DESC")
    fun getAllLockedFolders(): Flow<List<LockedFolderEntity>>
}

// ✅ Combine multiple flows for complex state
class GetDashboardDataUseCase @Inject constructor(
    private val fileRepo: FileRepository,
    private val vaultRepo: VaultRepository
) {
    operator fun invoke(): Flow<DashboardData> = combine(
        fileRepo.getStorageStats(),
        vaultRepo.getLockedFolderCount()
    ) { stats, lockedCount ->
        DashboardData(storageStats = stats, lockedFolderCount = lockedCount)
    }
}
```

### 6.2 ViewModel Flow Collection

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDashboardData: GetDashboardDataUseCase
) : ViewModel() {

    // ✅ stateIn() for hot state — survives collector lifecycle
    val dashboardState: StateFlow<DashboardUiState> = getDashboardData()
        .map { data -> DashboardUiState.Success(data) }
        .catch { e -> emit(DashboardUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000), // 5s timeout
            initialValue = DashboardUiState.Loading
        )
}
```

### 6.3 Flow Anti-Patterns

```kotlin
// ❌ NEVER: Collect Flow in init{} without lifecycle awareness
init {
    someFlow.collect { } // Blocks forever, leaks
}

// ❌ NEVER: Create new Flow on every recomposition
@Composable
fun BadExample(viewModel: MyViewModel) {
    val state = viewModel.getDataFlow().collectAsState() // New Flow each recomposition!
}

// ✅ CORRECT: Expose pre-created StateFlow
@Composable
fun GoodExample(viewModel: MyViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
}

// ❌ NEVER: Use collectAsState() — not lifecycle aware
val state = viewModel.state.collectAsState()

// ✅ ALWAYS: Use collectAsStateWithLifecycle()
val state by viewModel.state.collectAsStateWithLifecycle()
```

---

## 7. Lifecycle-Aware State Handling

### 7.1 WhileSubscribed Strategy

```kotlin
// ✅ Recommended: 5-second keepalive for configuration changes
val state: StateFlow<UiState> = dataFlow
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )
```

| Strategy | Use Case | Memory Impact |
|----------|----------|---------------|
| `WhileSubscribed(5_000)` | Default for all screens | Low — stops after 5s in background |
| `WhileSubscribed(0)` | Heavy data streams (video) | Lowest — stops immediately |
| `Eagerly` | App-wide settings, theme | Higher — always active |
| `Lazily` | One-time initialization | Medium — never stops once started |

### 7.2 Process Death Recovery

```kotlin
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getFolderContents: GetFolderContentsUseCase
) : ViewModel() {

    // ✅ Restore critical state after process death
    private val currentPath: String? = savedStateHandle.get<String>("folder_path")

    init {
        currentPath?.let { loadFolder(it) }
    }

    fun loadFolder(path: String) {
        savedStateHandle["folder_path"] = path
        // ... load logic
    }
}
```

---

## 8. Navigation State Handling

### 8.1 Type-Safe Navigation (Current Implementation)

```kotlin
// ✅ FileHive already uses Kotlin Serialization routes
@Serializable
data class Screen {
    @Serializable data object Splash : Screen
    @Serializable data object Home : Screen
    @Serializable data class Folder(val path: String) : Screen
    @Serializable data class ImageViewer(val filePath: String) : Screen
    @Serializable data object Search : Screen
    @Serializable data object RecycleBin : Screen
    @Serializable data object LockAuth : Screen
    @Serializable data object Vault : Screen
}
```

### 8.2 Navigation Effect Pattern

```kotlin
// ✅ Navigation as a UiEffect — ViewModel tells UI WHERE, UI handles HOW
sealed interface NavigationEffect {
    data class ToFolder(val path: String) : NavigationEffect
    data class ToViewer(val filePath: String) : NavigationEffect
    data object ToHome : NavigationEffect
    data object Back : NavigationEffect
}

// In ViewModel
private val _navigation = MutableSharedFlow<NavigationEffect>()
val navigation: SharedFlow<NavigationEffect> = _navigation.asSharedFlow()

// In Composable — collect and map to NavController actions
LaunchedEffect(Unit) {
    viewModel.navigation.collect { effect ->
        when (effect) {
            is NavigationEffect.ToFolder -> navController.navigate(Screen.Folder(effect.path))
            is NavigationEffect.ToViewer -> navController.navigate(Screen.ImageViewer(effect.filePath))
            is NavigationEffect.ToHome -> navController.navigate(Screen.Home) {
                popUpTo<Screen.Home> { inclusive = false }
                launchSingleTop = true
            }
            is NavigationEffect.Back -> navController.popBackStack()
        }
    }
}
```

### 8.3 Deep Link State Restoration

```kotlin
// For future: handle deep links into locked folders
composable<Screen.Folder>(
    deepLinks = listOf(
        navDeepLink { uriPattern = "filehive://folder/{path}" }
    )
) { backStackEntry ->
    val args = backStackEntry.toRoute<Screen.Folder>()
    FolderScreen(folderPath = args.path)
}
```

---

## 9. FileHive State Audit

### Current Issues & Recommended Fixes

| File | Issue | Fix |
|------|-------|-----|
| `FolderViewModel.kt` | Lives in `data/model/` | Move to `ui/screens/folder/` or `:feature:browser` |
| `FolderViewModel.kt` | Holds `currentFileList` as mutable var | Use `StateFlow<FolderUiState.Success>` derivation |
| `FolderViewModel.kt` | `lockFolder()` creates DB/repo inline | Inject via constructor (Hilt) |
| `LockAuthViewModel.kt` | Uses manual `ViewModelProvider.Factory` | Migrate to `@HiltViewModel` |
| `LockAuthViewModel.kt` | `_pinInput` as separate `StateFlow` | Consolidate into single `VaultScreenState` |
| `SecurityRepository.kt` | `hasPin` returns `flow { emit(...) }` | Return `Flow` from DataStore or use `callbackFlow` |
| `FolderStatsCache.kt` | Global `object` singleton | Convert to `@Singleton` Hilt binding |
| `VaultRepository.kt` | Takes raw `Context` in constructor | Inject `@ApplicationContext` via Hilt |

---

## 10. Testing State Management

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class FolderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: FolderViewModel
    private val fakeRepository = FakeFileRepository()

    @Before
    fun setup() {
        viewModel = FolderViewModel(
            getFolderContents = GetFolderContentsUseCase(fakeRepository)
        )
    }

    @Test
    fun `loadFolder emits Loading then Success`() = runTest {
        val files = listOf(FileItem("test.txt", 100L, false))
        fakeRepository.setFiles(files)

        viewModel.state.test {
            assertEquals(FolderUiState.Idle, awaitItem())

            viewModel.loadFolder("/test")

            assertEquals(FolderUiState.Loading, awaitItem())
            val success = awaitItem() as FolderUiState.Success
            assertEquals(1, success.fileCount)
        }
    }

    @Test
    fun `deleteFile performs optimistic update and rollback on failure`() = runTest {
        fakeRepository.shouldFail = true

        viewModel.state.test {
            // ... verify optimistic removal then rollback
        }
    }
}
```
