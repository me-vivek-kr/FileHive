# Known Issues

> Issue tracking, OEM compatibility, Android version-specific problems, and technical debt registry for FileHive.

---

## 1. Issue Tracking Template

### Bug Report Template

```markdown
## Bug Report

**ID**: FH-XXX
**Severity**: 🔴 Critical | 🟡 High | 🟢 Medium | 🔵 Low
**Component**: Browser | Vault | Search | Storage | Database | UI | Navigation
**Android Version(s)**: API XX (Android YY)
**Device(s)**: [Manufacturer Model]
**Status**: Open | In Progress | Fixed | Won't Fix | Deferred

### Description
[Clear, concise description of the bug]

### Steps to Reproduce
1. [First step]
2. [Second step]
3. [Expected vs. actual behavior]

### Expected Behavior
[What should happen]

### Actual Behavior
[What actually happens]

### Stack Trace / Logs
```
[Paste relevant logcat output]
```

### Screenshots / Videos
[Attach if applicable]

### Root Cause Analysis
[After investigation — what caused this]

### Fix Applied
[Branch, commit, approach]

### Regression Tests Added
- [ ] Unit test
- [ ] UI test
- [ ] Manual QA verified
```

### Feature Request Template

```markdown
## Feature Request

**ID**: FH-FR-XXX
**Priority**: 🔴 Must Have | 🟡 Should Have | 🟢 Nice to Have
**Component**: [Affected module]
**Status**: Proposed | Approved | In Development | Shipped

### User Story
As a [user type], I want [feature] so that [benefit].

### Acceptance Criteria
- [ ] [Criterion 1]
- [ ] [Criterion 2]
- [ ] [Criterion 3]

### Technical Notes
[Architecture considerations, dependencies, risks]

### Design Mockup
[Link or embedded image]
```

---

## 2. Active Known Issues

### 2.1 Critical Issues

| ID | Issue | Component | Impact | Workaround |
|----|-------|-----------|--------|------------|
| FH-001 | `fallbackToDestructiveMigration()` destroys user data on schema upgrade | Database | All locked folders lost on app update | Remove and add proper migrations before next release |
| FH-002 | Vault files stored in `getExternalFilesDir` — accessible via ADB/root | Security | Vault files exposed to determined attackers | Move to `context.filesDir` + add file encryption |
| FH-003 | PIN hashed with plain SHA-256 — vulnerable to brute force for 4-digit PINs | Security | PIN can be cracked in seconds with rainbow table | Upgrade to PBKDF2 with salt |
| FH-004 | `exportSchema = false` — no schema JSON for migration verification | Database | Cannot write reliable migration tests | Set to `true` and commit schema files |

### 2.2 High Priority Issues

| ID | Issue | Component | Impact | Workaround |
|----|-------|-----------|--------|------------|
| FH-005 | `FolderViewModel` lives in `data/model/` | Architecture | Violates layer separation, risks circular dependencies | Move to `ui/screens/folder/` |
| FH-006 | Manual DI — repositories instantiated inline in ViewModels | Architecture | Untestable, duplicated instances | Migrate to Hilt |
| FH-007 | `FolderViewModel.lockFolder()` creates `AppDatabase` + `VaultRepository` inline | Architecture | Multiple database instances, thread safety issues | Constructor inject via Hilt |
| FH-008 | No `.nomedia` file in vault directory | Storage | Vault folder thumbnails appear in Gallery app | Add `.nomedia` to vault dir on creation |
| FH-009 | `isMinifyEnabled = false` in release build | Security | No code obfuscation, easy to reverse engineer | Enable with proper ProGuard rules |
| FH-010 | `android:allowBackup` not set to `false` | Security | App data (including vault metadata) can be backed up and restored | Add to AndroidManifest |

### 2.3 Medium Priority Issues

| ID | Issue | Component | Impact | Workaround |
|----|-------|-----------|--------|------------|
| FH-011 | `currentFileList` is `mutableListOf` in ViewModel | State | Mutable state outside StateFlow; race conditions possible | Derive from `FolderUiState.Success` |
| FH-012 | `SecurityRepository.hasPin` uses `flow { emit() }` — not reactive | Data | PIN state changes not observed in real-time | Use `callbackFlow` or DataStore Flow |
| FH-013 | `FolderStatsCache` is a global `object` | Architecture | No DI, no testability, no lifecycle awareness | Convert to `@Singleton` class |
| FH-014 | `FileRepository` is an `object` (singleton) | Architecture | Cannot inject, cannot test, cannot mock | Convert to interface + implementation |
| FH-015 | Biometric library is alpha (`1.2.0-alpha05`) | Dependencies | Possible breaking changes on upgrade | Monitor stable release |
| FH-016 | `security-crypto` library is alpha (`1.1.0-alpha06`) | Dependencies | Key generation behavior may change | Pin version, monitor release |
| FH-017 | Recycle bin uses sidecar `.repo` files for metadata | Storage | Fragile — files can become orphaned | Migrate to Room table for bin metadata |
| FH-018 | No `FLAG_SECURE` on vault screens | Security | PIN entry / vault contents recordable via screenshot | Apply flag on vault composables |

### 2.4 Low Priority Issues

| ID | Issue | Component | Impact | Workaround |
|----|-------|-----------|--------|------------|
| FH-019 | No string resources — hardcoded strings in composables | i18n | Not localizable | Extract to `strings.xml` |
| FH-020 | No `@Preview` annotations on most composables | DX | Slower UI development iteration | Add previews with sample data |
| FH-021 | No unit tests exist | Testing | No regression safety | Add test suite |
| FH-022 | `sealed class` used instead of `sealed interface` | Code Style | Not idiomatic Kotlin 2.x | Migrate to `sealed interface` + `data object` |

---

## 3. OEM Compatibility Issues

### 3.1 Known OEM-Specific Problems

| OEM | Issue | Affected Versions | Workaround |
|-----|-------|-------------------|------------|
| **Samsung** | `File.renameTo()` fails across different storage volumes (Internal → SD card) | One UI 4.x+ | Use `copy()` + `delete()` instead |
| **Samsung** | Custom recycle bin conflicts with Samsung's built-in trash | One UI 5.x+ | Use unique bin directory name (`.FileHiveBin`) |
| **Xiaomi** | `MANAGE_EXTERNAL_STORAGE` permission revoked after app hibernation (MIUI optimization) | MIUI 14+ | Prompt user to exclude app from battery optimization |
| **Xiaomi** | AutoStart restriction blocks background file indexing | MIUI 12+ | Guide user to enable AutoStart in MIUI settings |
| **Huawei** | `BiometricPrompt` shows blank on some devices without Google Play Services | EMUI 12+ | Check `BiometricManager.canAuthenticate()` before showing |
| **OPPO/Realme** | ColorOS aggressively kills background services | ColorOS 12+ | Use `AlarmManager` with `setExactAndAllowWhileIdle()` |
| **OnePlus** | `StorageStatsManager` returns incorrect values for merged storage | OxygenOS 13 | Fall back to `StatFs` for storage calculations |
| **Vivo** | `MANAGE_EXTERNAL_STORAGE` permission dialog doesn't navigate to correct settings page | Funtouch OS 13 | Use `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` as fallback |

### 3.2 OEM-Specific Detection

```kotlin
object OemCompat {
    val isXiaomi: Boolean
        get() = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)

    val isSamsung: Boolean
        get() = Build.MANUFACTURER.equals("Samsung", ignoreCase = true)

    val isHuawei: Boolean
        get() = Build.MANUFACTURER.equals("Huawei", ignoreCase = true) ||
                Build.MANUFACTURER.equals("HONOR", ignoreCase = true)

    val isOppo: Boolean
        get() = Build.MANUFACTURER.equals("OPPO", ignoreCase = true) ||
                Build.MANUFACTURER.equals("Realme", ignoreCase = true)

    val isOnePlus: Boolean
        get() = Build.MANUFACTURER.equals("OnePlus", ignoreCase = true)

    fun needsAutoStartPermission(): Boolean = isXiaomi || isOppo || isHuawei

    fun canUseBiometrics(): Boolean {
        if (isHuawei && !isGooglePlayServicesAvailable()) return false
        return true
    }
}
```

---

## 4. Android Version-Specific Issues

### 4.1 API Level Compatibility Issues

| API | Issue | Affected Feature | Mitigation |
|-----|-------|-----------------|------------|
| 31 (12) | `MANAGE_EXTERNAL_STORAGE` requires special permission flow | File browsing | Already handled in `StoragePermissionGate` |
| 33 (13) | `Android/data/` of other apps blocked | File browsing | Show "Restricted" badge on these folders |
| 33 (13) | Per-photo picker required for new media permissions | Media import | Use `ActivityResultContracts.PickVisualMedia()` |
| 34 (14) | Predictive back gesture | Navigation | Implement `OnBackPressedCallback` |
| 34 (14) | Foreground service type required | File copy | Add `android:foregroundServiceType="dataSync"` |
| 35 (15) | Edge-to-edge enforced | All screens | Already using `enableEdgeToEdge()` |
| 35 (15) | `setStatusBarColor` deprecated | Theme | Use `WindowInsetsController` |
| 36 (16) | New privacy dashboard | Permissions | Document why MANAGE_EXTERNAL_STORAGE is needed |

### 4.2 Version-Specific Code

```kotlin
// ✅ Version-guarded feature implementation
fun openRestrictedDirectory(context: Context, path: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // API 33+: Cannot access Android/data of other apps
        // Show SAF picker instead
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        context.startActivity(intent)
    } else {
        // API 31-32: Direct access still works
        navigateToFolder(path)
    }
}

// ✅ Predictive back gesture support (API 34+)
@Composable
fun FileHiveBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = onBack)
    // Compose Navigation handles predictive back automatically with Nav 2.8+
}
```

---

## 5. Technical Debt Tracking

### 5.1 Debt Registry

| ID | Category | Description | Effort | Impact | Priority |
|----|----------|-------------|--------|--------|----------|
| TD-001 | Architecture | No domain/usecase layer | 3 days | High — business logic in ViewModels | 🔴 P1 |
| TD-002 | Architecture | No dependency injection framework | 2 days | High — untestable code | 🔴 P1 |
| TD-003 | Architecture | ViewModel in wrong package | 1 hour | Medium — confusing structure | 🟡 P2 |
| TD-004 | Security | Destructive migration enabled | 2 hours | Critical — data loss risk | 🔴 P0 |
| TD-005 | Security | Weak PIN hashing | 4 hours | High — brute force vulnerability | 🔴 P1 |
| TD-006 | Testing | Zero test coverage | 5 days | High — no regression safety | 🔴 P1 |
| TD-007 | Code Quality | Hardcoded strings | 2 days | Medium — blocks i18n | 🟡 P2 |
| TD-008 | Code Quality | No error handling in file operations | 1 day | Medium — silent failures | 🟡 P2 |
| TD-009 | Performance | No baseline profiles | 1 day | Low — slower cold start | 🟢 P3 |
| TD-010 | Build | No ProGuard/R8 in release | 2 hours | High — reverse engineering risk | 🔴 P1 |
| TD-011 | Architecture | Global `object` singletons | 1 day | Medium — untestable | 🟡 P2 |
| TD-012 | Data | Sidecar `.repo` files for recycle bin | 1 day | Medium — fragile metadata | 🟡 P2 |

### 5.2 Debt Burndown Priority

```
Sprint 1 (Critical/Blocking):
├── TD-004: Remove fallbackToDestructiveMigration
├── TD-010: Enable R8/ProGuard for release
└── TD-005: Upgrade PIN hashing to PBKDF2

Sprint 2 (Architecture Foundation):
├── TD-001: Introduce domain/usecase layer
├── TD-002: Integrate Hilt DI
└── TD-003: Move ViewModel to correct package

Sprint 3 (Quality):
├── TD-006: Add core test suite
├── TD-011: Replace object singletons with Hilt
└── TD-008: Add error handling

Sprint 4 (Polish):
├── TD-007: Extract strings to resources
├── TD-009: Generate baseline profiles
└── TD-012: Migrate recycle bin to Room
```

---

## 6. Issue Resolution Workflow

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  Report  │────▶│  Triage  │────▶│   Fix    │────▶│  Verify  │
│          │     │          │     │          │     │          │
│ - Repro  │     │ - Assign │     │ - Branch │     │ - Test   │
│ - Logs   │     │ - Label  │     │ - Code   │     │ - QA     │
│ - Device │     │ - Priority│    │ - Tests  │     │ - Close  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
```

### Labels

| Label | Meaning |
|-------|---------|
| `bug` | Confirmed defect |
| `enhancement` | Feature request |
| `tech-debt` | Code quality improvement |
| `security` | Security-related issue |
| `oem-compat` | Device/manufacturer-specific |
| `api-compat` | Android version-specific |
| `performance` | Performance regression or improvement |
| `breaking` | Potentially data-destructive change |
| `blocked` | Waiting on external dependency |
