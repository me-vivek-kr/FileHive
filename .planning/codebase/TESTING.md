# Testing

## Frameworks
- **Unit Testing**: Standard `junit:junit:4.13.2`. ViewModels and Repositories logic should be tested using mock data.
- **UI & Instrumentation**: `androidx.compose.ui:ui-test-junit4` for Compose interaction tests, falling back to `espresso-core` for legacy or systemic assertions. 

## Testing Patterns
- **Mocking**: Testing pure functions and standard state emissions via Kotlin Flow turbine or standard coroutine test dispatchers.
- Instrumented testing setup relies on `androidx.test.runner.AndroidJUnitRunner`.
- Coverage metrics are currently not visibly defined in the standard build scripts but standard Android Studio workflows apply.
