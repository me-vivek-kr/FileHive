# CI/CD Pipeline

> GitHub Actions pipelines for build, test, lint, static analysis, and release signing for FileHive.

---

## 1. Pipeline Overview

```
┌──────────────────────────────────────────────────────────────┐
│                     CI/CD PIPELINE                           │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Push / PR to main                                           │
│       │                                                      │
│       ▼                                                      │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐                 │
│  │  Build   │──▶│   Lint   │──▶│  Test    │                 │
│  │  Debug   │   │  Detekt  │   │  Unit    │                 │
│  └──────────┘   │  Ktlint  │   │  Tests   │                 │
│                 └──────────┘   └────┬─────┘                 │
│                                     │                        │
│                                     ▼                        │
│                              ┌──────────────┐                │
│                              │   Static     │                │
│                              │   Analysis   │                │
│                              │   (Detekt)   │                │
│                              └──────┬───────┘                │
│                                     │                        │
│                                     ▼                        │
│                              Pass? ─┬── No ──▶ ❌ Fail       │
│                                     │                        │
│                                Yes  ▼                        │
│                              ┌──────────────┐                │
│                              │  Upload      │                │
│                              │  Artifacts   │                │
│                              │  (Debug APK) │                │
│                              └──────────────┘                │
│                                                              │
│  Tag (v*.*.*)                                                │
│       │                                                      │
│       ▼                                                      │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐                 │
│  │  Build   │──▶│   Sign   │──▶│  Upload  │                 │
│  │  Release │   │   APK    │   │  to GH   │                 │
│  │  AAB     │   │   AAB    │   │  Release │                 │
│  └──────────┘   └──────────┘   └──────────┘                 │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Build Workflow

### 2.1 Main CI Workflow

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

env:
  JAVA_VERSION: '17'
  JAVA_DISTRIBUTION: 'temurin'

jobs:
  build:
    name: Build Debug
    runs-on: ubuntu-latest
    timeout-minutes: 20

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}

      - name: Build Debug APK
        run: ./gradlew assembleDebug --no-daemon

      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 14

  lint:
    name: Lint & Formatting
    runs-on: ubuntu-latest
    timeout-minutes: 15
    needs: build

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run Android Lint
        run: ./gradlew lintDebug --no-daemon

      - name: Upload Lint Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: app/build/reports/lint-results-debug.html
          retention-days: 7

  unit-test:
    name: Unit Tests
    runs-on: ubuntu-latest
    timeout-minutes: 15
    needs: build

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest --no-daemon

      - name: Upload Test Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: app/build/reports/tests/testDebugUnitTest/
          retention-days: 7

      - name: Upload Test Results (JUnit XML)
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: app/build/test-results/testDebugUnitTest/
          retention-days: 7
```

---

## 3. Static Analysis Workflow

### 3.1 Detekt Configuration

```yaml
# .github/workflows/static-analysis.yml
name: Static Analysis

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  detekt:
    name: Detekt Analysis
    runs-on: ubuntu-latest
    timeout-minutes: 10

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run Detekt
        run: ./gradlew detekt --no-daemon

      - name: Upload Detekt Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: detekt-report
          path: app/build/reports/detekt/
          retention-days: 7
```

### 3.2 Detekt Config File

```yaml
# config/detekt/detekt.yml
build:
  maxIssues: 0  # Zero tolerance — fail on any issue

complexity:
  LongMethod:
    threshold: 60
  LongParameterList:
    functionThreshold: 8
    constructorThreshold: 10
  ComplexCondition:
    threshold: 4
  TooManyFunctions:
    thresholdInFiles: 15
    thresholdInClasses: 15

naming:
  FunctionNaming:
    # Allow Compose naming convention (PascalCase for @Composable)
    excludes: ['**/ui/**']
  TopLevelPropertyNaming:
    constantPattern: '[A-Z][A-Za-z0-9_]*'

style:
  MagicNumber:
    ignoreNumbers: ['-1', '0', '1', '2', '3', '4', '5', '10', '100', '1000']
    ignorePropertyDeclaration: true
    ignoreAnnotation: true
    ignoreCompanionObjectPropertyDeclaration: true
  MaxLineLength:
    maxLineLength: 120
  WildcardImport:
    active: true
  UnusedImports:
    active: true
  ReturnCount:
    max: 4
    excludeGuardClauses: true

exceptions:
  TooGenericExceptionCaught:
    active: true
    exceptionNames:
      - Exception
    allowedExceptionNameRegex: '_|(ignore|expected).*'
  SwallowedException:
    active: true

performance:
  SpreadOperator:
    active: false  # Common in Compose

empty-blocks:
  EmptyCatchBlock:
    active: true
  EmptyFunctionBlock:
    active: true
    ignoreOverridden: true
```

### 3.3 Add Detekt to Build

```kotlin
// build.gradle.kts (root)
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

// build.gradle.kts (app)
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}
```

---

## 4. Linting Configuration

### 4.1 Android Lint Rules

```xml
<!-- app/lint.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- Treat these as errors -->
    <issue id="HardcodedText" severity="warning" />
    <issue id="MissingTranslation" severity="error" />
    <issue id="ObsoleteSdkInt" severity="warning" />
    <issue id="UnusedResources" severity="warning" />
    <issue id="ExtraTranslation" severity="error" />

    <!-- Security -->
    <issue id="HardcodedDebugMode" severity="error" />
    <issue id="AllowBackup" severity="error" />
    <issue id="SetJavaScriptEnabled" severity="error" />
    <issue id="ExportedContentProvider" severity="error" />

    <!-- Performance -->
    <issue id="Overdraw" severity="warning" />
    <issue id="UnusedIds" severity="warning" />

    <!-- Compose specific -->
    <issue id="ComposableLambdaParameterNaming" severity="warning" />
    <issue id="ComposableNaming" severity="error" />
    <issue id="RememberReturnType" severity="error" />

    <!-- Ignore for now -->
    <issue id="GradleDependency" severity="informational" />
    <issue id="NewerVersionAvailable" severity="informational" />
</lint>
```

### 4.2 Lint in Build Config

```kotlin
// app/build.gradle.kts
android {
    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
        lintConfig = file("lint.xml")
        htmlReport = true
        htmlOutput = file("build/reports/lint-results.html")
        xmlReport = true
        textReport = true
    }
}
```

---

## 5. APK/AAB Generation

### 5.1 Release Build Workflow

```yaml
# .github/workflows/release.yml
name: Release Build

on:
  push:
    tags:
      - 'v*.*.*'

permissions:
  contents: write

jobs:
  release:
    name: Build & Release
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Extract version from tag
        id: version
        run: echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT

      - name: Decode Keystore
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/release-keystore.jks

      - name: Build Release APK
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease --no-daemon

      - name: Build Release AAB
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease --no-daemon

      - name: Upload Release APK
        uses: actions/upload-artifact@v4
        with:
          name: release-apk-${{ steps.version.outputs.VERSION }}
          path: app/build/outputs/apk/release/app-release.apk
          retention-days: 90

      - name: Upload Release AAB
        uses: actions/upload-artifact@v4
        with:
          name: release-aab-${{ steps.version.outputs.VERSION }}
          path: app/build/outputs/bundle/release/app-release.aab
          retention-days: 90

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          name: FileHive v${{ steps.version.outputs.VERSION }}
          generate_release_notes: true
          files: |
            app/build/outputs/apk/release/app-release.apk
            app/build/outputs/bundle/release/app-release.aab

      - name: Cleanup Keystore
        if: always()
        run: rm -f app/release-keystore.jks
```

---

## 6. Release Signing

### 6.1 Keystore Generation

```bash
# Generate release keystore (one-time setup)
keytool -genkey -v \
  -keystore release-keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias filehive-release \
  -storepass <STORE_PASSWORD> \
  -keypass <KEY_PASSWORD> \
  -dname "CN=FileHive, OU=Mobile, O=Viv3k, L=City, ST=State, C=IN"
```

### 6.2 Signing Config

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("release-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 6.3 GitHub Secrets Setup

```
Repository → Settings → Secrets and variables → Actions

Required Secrets:
├── KEYSTORE_BASE64     ← base64 -i release-keystore.jks | pbcopy
├── KEYSTORE_PASSWORD   ← Keystore password
├── KEY_ALIAS           ← Key alias (filehive-release)
└── KEY_PASSWORD        ← Key password
```

### 6.4 ProGuard Rules

```proguard
# proguard-rules.pro

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Kotlin Serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# FileHive data classes used by Room/Serialization
-keep class com.viv3k.filehive.data.database.** { *; }
-keep class com.viv3k.filehive.data.model.** { *; }

# Coil
-dontwarn coil3.**

# EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# Compose Navigation routes
-keep class com.viv3k.filehive.core.navigation.Screen { *; }
-keep class com.viv3k.filehive.core.navigation.Screen$* { *; }

# Keep source file names for crash reporting
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
```

---

## 7. Deployment Workflow

### 7.1 Release Process

```
1. Feature Development
   └── Feature branch → PR → Review → Merge to develop

2. Release Preparation
   └── develop → release/vX.Y.Z branch
   └── Update versionCode + versionName in build.gradle.kts
   └── Update CHANGELOG.md
   └── Final QA testing

3. Release
   └── Merge release/vX.Y.Z → main
   └── Tag: git tag -a vX.Y.Z -m "Release vX.Y.Z"
   └── Push tag: git push origin vX.Y.Z
   └── CI builds + signs + creates GitHub Release

4. Post-Release
   └── Merge main → develop (sync release changes)
   └── Monitor crash reports
   └── Prepare hotfix branch if needed
```

### 7.2 Version Naming Convention

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 1        // Increment for every release (1, 2, 3, ...)
        versionName = "1.0.0"  // Semantic versioning: MAJOR.MINOR.PATCH
    }
}
```

| Version Part | When to Bump | Example |
|-------------|-------------|---------|
| MAJOR | Breaking changes, major redesign | 1.0.0 → 2.0.0 |
| MINOR | New features, backward compatible | 1.0.0 → 1.1.0 |
| PATCH | Bug fixes, minor improvements | 1.0.0 → 1.0.1 |

### 7.3 CHANGELOG Template

```markdown
# Changelog

## [1.1.0] - 2026-06-XX

### Added
- Full-text file search with FTS4 index
- File category breakdown on home screen
- SAF support for SD card access

### Changed
- Upgraded Room to 2.7.0
- Migrated from manual DI to Hilt
- Moved FolderViewModel to correct package

### Fixed
- FH-001: Database no longer destroyed on schema upgrade
- FH-008: Vault folder no longer appears in Gallery

### Security
- FH-003: PIN hashing upgraded to PBKDF2
- FH-010: R8 minification enabled for release builds
```

---

## 8. Instrumented Test Workflow (Future)

```yaml
# .github/workflows/instrumented-tests.yml
name: Instrumented Tests

on:
  pull_request:
    branches: [main]

jobs:
  android-test:
    name: Instrumented Tests
    runs-on: ubuntu-latest
    timeout-minutes: 45

    strategy:
      matrix:
        api-level: [31, 33, 35]

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run Instrumented Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          arch: x86_64
          target: google_apis
          script: ./gradlew connectedDebugAndroidTest --no-daemon

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: instrumented-test-results-api${{ matrix.api-level }}
          path: app/build/reports/androidTests/connected/
          retention-days: 7
```

---

## 9. CI Pipeline Summary

| Trigger | Workflow | Jobs | Duration |
|---------|----------|------|----------|
| Push to `main`/`develop` | CI | Build + Lint + Unit Tests | ~8 min |
| Pull Request to `main` | CI + Static Analysis | Build + Lint + Tests + Detekt | ~12 min |
| Tag `v*.*.*` | Release Build | Build + Sign + GitHub Release | ~15 min |
| PR to `main` (future) | Instrumented Tests | Emulator tests on API 31/33/35 | ~45 min |

### Required GitHub Actions Secrets

| Secret | Purpose |
|--------|---------|
| `KEYSTORE_BASE64` | Base64-encoded release keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Signing key alias |
| `KEY_PASSWORD` | Signing key password |
