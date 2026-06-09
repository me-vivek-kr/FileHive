# Code Structure

The project uses feature-based and layer-based hybrid grouping under the main package `com.viv3k.filehive`.

- `app/src/main/java/com/viv3k/filehive/`
  - `core/`: Application-wide setup, utilities, and integrations.
    - `coil/`: Custom image/video loader setups.
    - `navigation/`: Main routing logic (`AppNavigation.kt`).
  - `data/`: The entire data layer (Repositories, DAOs, Storage logic).
    - `common/`: Shared models or constants.
    - `database/`: Room setup (`AppDatabase`, DAOs, Entities).
    - `model/`: Domain and UI models, along with ViewModels attached to specific data slices (`FolderViewModel`).
    - `repository/`: Gateway to data sources (`FileRepository`, `VaultRepository`, `SecurityRepository`).
    - `storage/`: Platform-specific file access APIs.
  - `ui/`: The UI layer organized by feature and reusable components.
    - `screens/`: Contains feature modules: (`folder`, `homescreen`, `recyclebin`, `search`, `vault`, `viewer`).
    - `components/`: Generic, cross-feature composables.
    - `theme/`: Typography, colors, and Theme composition wrapper.
