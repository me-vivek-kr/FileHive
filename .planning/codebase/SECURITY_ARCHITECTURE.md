# Security Architecture

> End-to-end security design for FileHive's vault, encryption, biometrics, and threat modeling — built for Android 12+ (API 31+).

---

## 1. Security Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        USER INTERFACE                            │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────┐     │
│  │  PIN Pad    │  │  Biometric   │  │  Security Question  │     │
│  │  (4-digit)  │  │  Prompt      │  │  Recovery Flow      │     │
│  └──────┬──────┘  └──────┬───────┘  └──────────┬──────────┘     │
└─────────┼────────────────┼──────────────────────┼────────────────┘
          │                │                      │
          ▼                ▼                      ▼
┌──────────────────────────────────────────────────────────────────┐
│                     AUTHENTICATION LAYER                         │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │              LockAuthViewModel                            │    │
│  │  • PIN state machine (SETUP → CONFIRM → UNLOCK)          │    │
│  │  • Lockout enforcement (5 attempts → 30s cooldown)       │    │
│  │  • Biometric callback handler                            │    │
│  │  • Security question verification                        │    │
│  └──────────────────────────┬───────────────────────────────┘    │
└─────────────────────────────┼────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                      SECURITY LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │ Security     │  │ Vault        │  │  Biometric           │   │
│  │ Repository   │  │ Repository   │  │  Auth Manager        │   │
│  │ (PIN, Q&A)   │  │ (Lock/       │  │  (System Biometric   │   │
│  │              │  │  Unlock)     │  │   Prompt)            │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────────────────┘   │
└─────────┼─────────────────┼──────────────────────────────────────┘
          │                 │
          ▼                 ▼
┌──────────────────────────────────────────────────────────────────┐
│                      CRYPTO LAYER                                │
│  ┌──────────────────────┐  ┌─────────────────────────────────┐   │
│  │  Android Keystore    │  │  EncryptedSharedPreferences     │   │
│  │  AES-256-GCM         │  │  (PIN hash, security Q&A)      │   │
│  │  MasterKey           │  │  AES256_SIV key encryption     │   │
│  └──────────────────────┘  │  AES256_GCM value encryption   │   │
│                            └─────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
          │                 │
          ▼                 ▼
┌──────────────────────────────────────────────────────────────────┐
│                      STORAGE LAYER                               │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │  Hidden Vault Directory: getExternalFilesDir(null)/.vault │   │
│  │  • Folders moved via File.renameTo()                      │   │
│  │  • UUID-based naming (no original path leakage)           │   │
│  │  • Room metadata: locked_folders table                    │   │
│  └───────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 2. Vault Security Flow

### 2.1 First-Time Setup Flow

```
User opens Vault → No PIN exists
    │
    ▼
[SETUP_PIN] → User enters 4-digit PIN
    │
    ▼
[CONFIRM_PIN] → User re-enters same PIN
    │
    ├── Mismatch → Back to SETUP_PIN
    │
    ▼ Match
[SETUP_SECURITY_QUESTION] → User sets Q&A
    │
    ▼
PIN hashed (SHA-256) → Stored in EncryptedSharedPreferences
Security Q hashed  → Stored in EncryptedSharedPreferences
    │
    ▼
[UNLOCKED] → User can access vault
```

### 2.2 Unlock Flow

```
User opens Vault → PIN exists
    │
    ▼
[UNLOCK] → Enter PIN
    │
    ├── Correct → [UNLOCKED] ✅
    │
    ├── Incorrect (< 5 attempts) → Show error, retry
    │
    └── Incorrect (5th attempt) → [LOCKED_OUT] for 30 seconds
         │
         └── Countdown timer → Retry after lockout
```

### 2.3 Forgot Password Flow

```
[UNLOCK] → "Forgot Password?" tap
    │
    ▼
Load security question from EncryptedSharedPreferences
    │
    ▼
[FORGOT_PASSWORD] → User enters answer
    │
    ├── Correct → resetPin() → [SETUP_PIN] (set new PIN)
    │
    └── Incorrect → Show error
```

### 2.4 Biometric Authentication

```
[UNLOCK] → Biometric icon tap
    │
    ▼
BiometricPrompt.authenticate()
    │
    ├── SUCCESS → onBiometricSuccess() → [UNLOCKED]
    │
    ├── ERROR → Show error, fall back to PIN
    │
    └── FAILED → Increment attempt counter
```

---

## 3. Android Keystore Usage

### 3.1 Current Implementation

FileHive uses `MasterKey` from `androidx.security:security-crypto:1.1.0-alpha06`:

```kotlin
// Current: SecurityRepository.kt
private val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()
```

### 3.2 Recommended Production Keystore Strategy

```kotlin
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ✅ Primary MasterKey for EncryptedSharedPreferences
    val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false) // Change to true for biometric-bound keys
            .build()
    }

    // ✅ Biometric-bound key for sensitive operations
    fun createBiometricBoundKey(): MasterKey {
        return MasterKey.Builder(context, "biometric_master_key")
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(true)
            .setRequestStrongBoxBacked(true) // Hardware-backed if available
            .build()
    }

    // ✅ Verify key integrity on app launch
    fun verifyKeyIntegrity(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Key rotation strategy
    fun rotateKeys() {
        // 1. Decrypt all data with old key
        // 2. Generate new MasterKey
        // 3. Re-encrypt all data with new key
        // 4. Delete old key alias from Keystore
    }
}
```

### 3.3 Key Specifications

| Key | Algorithm | Purpose | Storage |
|-----|-----------|---------|---------|
| `_androidx_security_master_key_` | AES-256-GCM | Encrypt SharedPreferences | Android Keystore |
| PIN Hash | SHA-256 | PIN verification | EncryptedSharedPreferences |
| Security Answer Hash | SHA-256 | Recovery verification | EncryptedSharedPreferences |
| `biometric_master_key` (planned) | AES-256-GCM | Biometric-bound operations | Android Keystore (StrongBox) |

---

## 4. Encryption Architecture

### 4.1 Current Encryption Layers

```
Layer 1: EncryptedSharedPreferences
├── Key Encryption: AES256_SIV (deterministic, key name encryption)
├── Value Encryption: AES256_GCM (authenticated encryption)
└── Keys Stored:
    ├── pin_hash (SHA-256 of user PIN)
    ├── security_question (plaintext question)
    ├── answer_hash (SHA-256 of lowercase trimmed answer)
    ├── fail_count (integer)
    └── lockout_time (timestamp)

Layer 2: File System Obscurity
├── Vault directory: getExternalFilesDir(null)/.vault/
├── Files renamed to UUID (no original names exposed)
└── Original paths stored in Room DB (locked_folders table)
```

### 4.2 Recommended: File-Level Encryption (Phase 2)

```kotlin
// ✅ Future: Encrypt actual file contents in vault
class CryptoEngine @Inject constructor(
    private val keystoreManager: KeystoreManager
) {
    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 128
    }

    fun encryptFile(inputFile: File, outputFile: File) {
        val key = keystoreManager.getFileEncryptionKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        outputFile.outputStream().use { output ->
            output.write(iv) // Prepend IV
            CipherInputStream(inputFile.inputStream(), cipher).use { cipherInput ->
                cipherInput.copyTo(output)
            }
        }
    }

    fun decryptFile(inputFile: File, outputFile: File) {
        val key = keystoreManager.getFileEncryptionKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        inputFile.inputStream().use { input ->
            val iv = ByteArray(IV_SIZE)
            input.read(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE, iv))

            CipherOutputStream(outputFile.outputStream(), cipher).use { cipherOutput ->
                input.copyTo(cipherOutput)
            }
        }
    }
}
```

### 4.3 PIN Hashing Improvements

```kotlin
// ⚠️ Current: Plain SHA-256 (fast, vulnerable to brute force for 4-digit PINs)
private fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

// ✅ Recommended: Argon2 or PBKDF2 with salt
class HashUtils {
    companion object {
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH = 256

        fun hashPin(pin: String, salt: ByteArray): String {
            val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            return Base64.encodeToString(salt + hash, Base64.NO_WRAP)
        }

        fun generateSalt(): ByteArray {
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            return salt
        }

        fun verifyPin(pin: String, storedHash: String): Boolean {
            val decoded = Base64.decode(storedHash, Base64.NO_WRAP)
            val salt = decoded.sliceArray(0 until 16)
            val expectedHash = hashPin(pin, salt)
            return MessageDigest.isEqual(
                decoded,
                Base64.decode(expectedHash, Base64.NO_WRAP)
            )
        }
    }
}
```

---

## 5. Biometrics Integration

### 5.1 Current Implementation

FileHive uses `androidx.biometric:biometric:1.2.0-alpha05` with `FragmentActivity`.

### 5.2 Production Biometric Manager

```kotlin
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    sealed interface BiometricResult {
        data object Success : BiometricResult
        data class Error(val code: Int, val message: String) : BiometricResult
        data object NotAvailable : BiometricResult
        data object NotEnrolled : BiometricResult
        data object HardwareUnavailable : BiometricResult
    }

    fun canAuthenticate(): BiometricResult {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricResult.Success
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricResult.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricResult.NotEnrolled
            else -> BiometricResult.NotAvailable
        }
    }

    fun createPromptInfo(
        title: String = "Unlock FileHive Vault",
        subtitle: String = "Use your fingerprint to access locked folders",
        negativeButtonText: String = "Use PIN"
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .build()
    }

    fun authenticate(
        activity: FragmentActivity,
        promptInfo: BiometricPrompt.PromptInfo,
        onResult: (BiometricResult) -> Unit
    ) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(BiometricResult.Error(errorCode, errString.toString()))
            }

            override fun onAuthenticationFailed() {
                // Don't call onResult — biometric failed but user can retry
            }
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
}
```

### 5.3 Biometric + Cryptography Binding (Phase 2)

```kotlin
// ✅ Bind biometric auth to crypto operation — strongest security
fun authenticateWithCrypto(
    activity: FragmentActivity,
    onResult: (BiometricResult, Cipher?) -> Unit
) {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    val key = keyStore.getKey("biometric_vault_key", null) as SecretKey

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, key)

    val cryptoObject = BiometricPrompt.CryptoObject(cipher)
    val prompt = BiometricPrompt(activity, executor, callback)
    prompt.authenticate(promptInfo, cryptoObject) // Key only usable after bio auth
}
```

---

## 6. Secure File Handling

### 6.1 Vault Directory Strategy

```kotlin
// Current: Files moved to app-private external storage
val vaultDir = File(context.getExternalFilesDir(null), ".vault")

// Pros:
// ✅ Not visible in MediaStore
// ✅ Cleared on app uninstall
// ✅ UUID naming prevents name leakage

// Cons:
// ⚠️ Accessible via ADB on debuggable builds
// ⚠️ No encryption of actual file bytes
// ⚠️ Root access can read files
```

### 6.2 Recommended Improvements

| Current | Recommended | Priority |
|---------|-------------|----------|
| `getExternalFilesDir(null)/.vault` | `context.filesDir/.vault` (internal storage) | 🔴 High |
| Files stored as-is (no encryption) | AES-256-GCM file encryption | 🟡 Medium |
| `File.renameTo()` for move | Copy + encrypt + secure delete original | 🟡 Medium |
| No `.nomedia` file | Add `.nomedia` to prevent media indexing | 🔴 High |
| Original path in Room (plaintext) | Encrypt original path in DB | 🟢 Low |

### 6.3 Secure Delete

```kotlin
// ✅ Overwrite file data before deletion
fun secureDelete(file: File): Boolean {
    if (!file.exists()) return false

    return try {
        val length = file.length()
        RandomAccessFile(file, "rw").use { raf ->
            val random = SecureRandom()
            val buffer = ByteArray(4096)

            // Pass 1: Random data
            raf.seek(0)
            var remaining = length
            while (remaining > 0) {
                random.nextBytes(buffer)
                val toWrite = minOf(remaining, buffer.size.toLong()).toInt()
                raf.write(buffer, 0, toWrite)
                remaining -= toWrite
            }

            // Pass 2: Zeros
            raf.seek(0)
            remaining = length
            buffer.fill(0)
            while (remaining > 0) {
                val toWrite = minOf(remaining, buffer.size.toLong()).toInt()
                raf.write(buffer, 0, toWrite)
                remaining -= toWrite
            }
        }
        file.delete()
    } catch (e: Exception) {
        file.delete() // Best-effort delete on failure
    }
}
```

---

## 7. Threat Modeling

### 7.1 STRIDE Analysis for FileHive Vault

| Threat | Category | Attack Vector | Current Mitigation | Recommended Mitigation |
|--------|----------|---------------|-------------------|----------------------|
| PIN brute force | Spoofing | Try all 10,000 4-digit combos | 5-attempt lockout (30s) | PBKDF2 hashing + exponential backoff + wipe after 15 attempts |
| Shoulder surfing | Spoofing | Watch user enter PIN | None | Randomized keypad layout option |
| ADB file extraction | Tampering | `adb pull` vault directory | Files in app-specific external dir | Move to internal storage + file encryption |
| Root access | Tampering | Root shell reads vault files | None | AES-256-GCM file encryption |
| Memory dump | Information Disclosure | Dump process memory for PIN | PIN stored as StateFlow String | Clear PIN from memory after verification, use `CharArray` |
| DB path leakage | Information Disclosure | Read Room DB for original file paths | None | Encrypt sensitive DB columns |
| App clone/backup | Spoofing | Backup app data + restore | None | `android:allowBackup="false"` in manifest |
| Screen recording | Information Disclosure | Record screen during vault access | None | `FLAG_SECURE` on vault screens |
| Lockout bypass | Elevation of Privilege | Clear app data to reset lockout | Counter in EncryptedSharedPreferences | Track in server or use Keystore-backed counter |

### 7.2 Risk Matrix

```
                    ┌──────────────────────────────────────────┐
                    │           IMPACT                         │
                    │   Low        Medium       High           │
    ┌───────────────┼──────────────────────────────────────────┤
    │ High          │            Screen        Root access     │
    │               │            recording     file theft      │
L   │               │                                         │
I   ├───────────────┼──────────────────────────────────────────┤
K   │ Medium        │ Shoulder    ADB          Memory          │
E   │               │ surfing     extraction   dump            │
L   │               │                                         │
I   ├───────────────┼──────────────────────────────────────────┤
H   │ Low           │ App        DB path       PIN brute       │
O   │               │ clone      leakage       force           │
O   │               │                                         │
D   └───────────────┴──────────────────────────────────────────┘
```

---

## 8. Rooted Device Considerations

### 8.1 Root Detection Strategy

```kotlin
class RootDetector @Inject constructor() {

    fun isDeviceRooted(): Boolean {
        return checkSuBinary() || checkSuCommand() || checkMagisk() || checkBuildTags()
    }

    private fun checkSuBinary(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkBuildTags(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    private fun checkMagisk(): Boolean {
        return File("/sbin/.magisk").exists() ||
               File("/cache/.disable_magisk").exists()
    }

    private fun checkSuCommand(): Boolean {
        return try {
            Runtime.getRuntime().exec("su").let { process ->
                process.destroy()
                true
            }
        } catch (e: Exception) { false }
    }
}
```

### 8.2 Root Policy Options

| Policy | Behavior | Recommended For |
|--------|----------|-----------------|
| **Warn** | Show warning banner, allow usage | Default for FileHive |
| **Restrict Vault** | Disable vault on rooted devices | High-security users |
| **Block** | Refuse to launch on rooted devices | Enterprise deployments |

```kotlin
// ✅ Recommended: Warn on rooted device, let user decide
@Composable
fun RootWarningBanner(isRooted: Boolean) {
    if (isRooted) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️ Rooted device detected. Vault security may be compromised.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

---

## 9. Secure Preferences Strategy

### 9.1 Current: EncryptedSharedPreferences

```kotlin
// ✅ Already implemented in SecurityRepository.kt
private val prefs = EncryptedSharedPreferences.create(
    context,
    "secure_vault_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 9.2 Recommended: Dual Preference Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                  PREFERENCE STRATEGY                         │
├─────────────────────────────┬───────────────────────────────┤
│  DataStore Preferences      │  EncryptedSharedPreferences   │
│  (Non-sensitive settings)   │  (Sensitive security data)    │
├─────────────────────────────┼───────────────────────────────┤
│  • Theme preference         │  • PIN hash + salt            │
│  • View mode (list/grid)    │  • Security question          │
│  • Sort order               │  • Security answer hash       │
│  • Last opened folder       │  • Failed attempt count       │
│  • Onboarding completed     │  • Lockout timestamp          │
│  • App language             │  • Biometric enrollment flag  │
│                             │  • Encryption key metadata    │
└─────────────────────────────┴───────────────────────────────┘
```

### 9.3 Secure Preferences Manager

```kotlin
@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager
) {
    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "filehive_secure_prefs",
            keystoreManager.masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ✅ Type-safe accessors
    var pinHash: String?
        get() = securePrefs.getString("pin_hash", null)
        set(value) = securePrefs.edit().putString("pin_hash", value).apply()

    var failedAttempts: Int
        get() = securePrefs.getInt("fail_count", 0)
        set(value) = securePrefs.edit().putInt("fail_count", value).apply()

    fun clearAllSecurityData() {
        securePrefs.edit().clear().apply()
    }
}
```

---

## 10. Security Checklist

### Pre-Launch Security Audit

| Item | Status | Priority |
|------|--------|----------|
| `android:allowBackup="false"` in AndroidManifest | ⬜ TODO | 🔴 Critical |
| `FLAG_SECURE` on vault screens | ⬜ TODO | 🔴 Critical |
| Move vault to internal storage (`context.filesDir`) | ⬜ TODO | 🔴 Critical |
| Add `.nomedia` to vault directory | ⬜ TODO | 🔴 Critical |
| Upgrade PIN hashing from SHA-256 to PBKDF2 | ⬜ TODO | 🔴 Critical |
| Clear PIN from memory (`CharArray.fill(0)`) after use | ⬜ TODO | 🟡 High |
| Add exponential lockout backoff | ⬜ TODO | 🟡 High |
| ProGuard/R8 obfuscation enabled for release builds | ⬜ TODO | 🟡 High |
| Certificate pinning for future network calls | ⬜ TODO | 🟢 Low |
| Root detection with configurable policy | ⬜ TODO | 🟢 Low |
| File-level AES-256-GCM encryption | ⬜ TODO | 🟡 High |
| Biometric-bound crypto key | ⬜ TODO | 🟢 Low |
| Security event audit logging | ⬜ TODO | 🟢 Low |
| Penetration testing | ⬜ TODO | 🔵 Pre-launch |

### Manifest Hardening

```xml
<application
    android:allowBackup="false"
    android:fullBackupContent="false"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false">

    <!-- Vault activity must prevent screenshots -->
    <activity
        android:name=".MainActivity"
        android:exported="true">
        <!-- FLAG_SECURE applied programmatically for vault screens -->
    </activity>
</application>
```
