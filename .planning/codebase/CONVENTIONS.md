# Conventions

## Coding Style
- **Language**: Kotlin 2.1.0 with idiomatic idioms, leveraging extensive Coroutines logic.
- **Immutability**: Emphasize immutable data structures (`data class` and `val`) in models.

## Jetpack Compose Guidelines
- Compose features are divided into UI components (`components/`) and screen representations (`screens/`).
- State is hoisted where possible, utilizing standard ViewModel injection (`viewModel()`) at the screen level to reduce coupling inside isolated UI segments.
- Preview annotations (`@Preview`) combined with sample data are highly suggested to preview UI blocks rapidly.

## Architecture Guidelines
- **Dependency Injection**: Currently manual instantiation or relying on ViewModel factories if Dagger/Hilt is not directly applied yet.
- **Error Handling**: Use Kotlin `Result` generic types or sealed classes for State emission (`Loading`, `Success`, `Error`) to model repository outcomes accurately to the ViewModels.

## Build Scripts
- **Gradle KTS**: `build.gradle.kts` configuration logic.
- Version management is centralized in `gradle/libs.versions.toml`.
