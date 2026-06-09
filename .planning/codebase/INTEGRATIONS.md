# Integrations

## External Services
- Currently, this is a local offline File Manager app (FileHive) with no external cloud API integrations specified in the core dependencies.

## Key Subsystems & Hardware
- **Local Storage / Filesystem**: Direct access to Android local storage directories via FileRepository. Requires scoped storage permissions depending on Android version.
- **Biometric Prompt**: Integrates with Android's secure subsystem (`androidx.biometric`) to authenticate users (fingerprint/face) for unlocking secure folders or the app vault.
- **Room SQLite**: Local caching and metadata persistence for folders.

## Auth Providers
- **Local Authentication**: PIN, Security Questions, and Biometrics (via `SecurityRepository` and `VaultRepository`).
