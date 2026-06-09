# Concerns & Tech Debt

## Security & Vault Design
- Reverting to pin/sequence implies we must consider local side-channel attacks for locked folders. The Vault locking mechanism and `LockedFolderDao` implementations need robust validation against direct runtime tampering.
- Using `androidx.biometric:biometric:1.2.0-alpha05` (an alpha version), which may bring stability issues or breaking changes in the future when upgrading to beta/stable.

## Performance
- **Folder Caching**: Managing caching entities (`FolderCacheEntity`) implies potential memory bottlenecks if users have millions of files. The folder metadata cache invalidation logic requires regular grooming or expiration to avoid stale data displaying in the fast UI loop.

## Architecture Debt
- ViewModel files like `FolderViewModel.kt` live inside `data/model/`, which creates an architectural layering overlap (ViewModel is traditionally part of the Presentation tier, not Data). This might cause cyclic dependency risk if not structured carefully.
