# Architecture

The application is built leveraging **Clean Architecture** concepts combined with **MVVM** (Model-View-ViewModel) suitable for Jetpack Compose apps.

## Presentation Layer
- **UI Framework**: Jetpack Compose.
- **State Management**: ViewModels (e.g., `FolderViewModel`) hold UI state, often exposing standard Kotlin Flows or `StateFlow` to composables. State hoisting is practiced to keep composables stateless.
- **Navigation**: Managed centrally through `core/navigation/AppNavigation.kt`. Routing uses Kotlin Serialization if passing complex arguments, though standard String routes are common. Features are isolated in `ui/screens`.

## Domain / Data Layer
- **Repositories**: (`data/repository/`). Repositories like `FileRepository`, `VaultRepository`, and `SecurityRepository` define the data access strategies, acting as the single source of truth for the application's ViewModels.
- **Data Sources**:
  - Local Database: Accessed via Room DAOs (`LockedFolderDao`, `FolderCacheDao`).
  - File System: Direct file IO or MediaStore wrapping to access device files (`data/storage/`).
  - Preferences: Datastore for application-wide settings and security tokens.

## Diagram / Flow
1. **User Action** triggers an event in the Compose UI (`ui/screens`).
2. Event is routed to the corresponding **ViewModel** (`data/model`).
3. ViewModel communicates with the **Repository** to process data.
4. Repository uses Room logic (`data/database`) or standard File IO.
5. Result flows back up to UI, which recomposes.
