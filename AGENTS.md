# AGENTS.md - Project Intelligence for AI Assistants

## Project Context
**ContactPicker** is a Kotlin Multiplatform (KMP) library that provides a native contact selection experience for Android and iOS using Jetpack Compose Multiplatform. It abstracts platform-specific contact APIs (ContactsContract on Android and Contacts framework on iOS) into a unified, reactive Compose-friendly API.

## Architecture Map
The project follows a **Layered Architecture** with a clear separation between common API definitions and platform-specific implementations using Kotlin's `expect/actual` mechanism.

- **`:contactpicker`**: The core library module.
    - `commonMain`: Defines the public API, data models, and state management logic.
    - `androidMain`: Android-specific implementation using Activity Result API and ContentResolver.
    - `iosMain`: iOS-specific implementation using `CNContactPickerViewController` and Objective-C/Swift interop.
- **`:composeApp`**: A sample/demo application demonstrating library usage.

## Layer Responsibilities

### 1. Data Layer (`com.devtamuno.kmp.contactpicker.data`)
- **`Contact`**: An `@Immutable` data class representing the cross-platform contact model.
- Includes custom `equals` and `hashCode` for `ByteArray` (avatar) support.

### 2. Contract Layer (`com.devtamuno.kmp.contactpicker.contract`)
- **`ContactPickerState`**: Interface defining the reactive state (`value: State<Contact?>`) and actions (`launchContactPicker()`).
- **`ContactPicker` (expect/actual)**: Low-level platform bridge for registering and launching the native picker.
- **`ContactPickerStateImpl`**: Internal implementation coordinating the state updates.

### 3. Extension Layer (`com.devtamuno.kmp.contactpicker.extension`)
- **`BitmapConverter`**: Decodes raw `ByteArray` avatars into platform-specific `ImageBitmap`.

## Coding Standards

### 1. Naming Conventions
- **Expect/Actual**: Use the same name for `expect` and `actual` classes.
- **State Management**: Use `rememberContactPickerState` as the public entry point for Composable state.
- **Internal Visibility**: Classes not intended for public use should be marked `internal` or `private`.

### 2. Code Style Patterns
- **Reactive State**: Prefer `androidx.compose.runtime.State` for exposing data to UI.
- **Lateinit Check**: Use `if (::picker.isInitialized) return` in `RegisterContactPicker` to prevent redundant initialization during recomposition.
- **Cleanup**: Always set delegates to `null` and dismiss controllers/launchers after use.

### 3. Documentation (KDoc)
- Use professional, descriptive KDoc for all public and internal APIs.
- Document platform-specific nuances (e.g., fallback mechanisms for avatars on Android).

## Dependency Rules
- **Gradle Version Catalogs**: All dependencies are managed in `gradle/libs.versions.toml`.
- **Minimal Dependencies**: The library aims to be lightweight, relying primarily on Compose Multiplatform and Kotlin Standard Library.
- **Maven Publishing**: Configured using `com.vanniktech.maven.publish`.

## Platform Constraints

### Android
- Requires `android.permission.READ_CONTACTS`.
- Uses `rememberLauncherForActivityResult` with `PickContact` contract.
- Queries `ContentResolver` for detailed contact info (phones, emails, photos).

### iOS
- Requires `NSContactsUsageDescription` in `Info.plist`.
- Uses `CNContactPickerViewController` from the `ContactsUI` framework.
- Uses `memScoped` and `memcpy` for efficient `NSData` to `ByteArray` conversion.

## Testing Expectations
- Currently, the project lacks automated tests. **New feature additions should include `commonTest` where possible.**

## Anti-patterns to Avoid
- **Context Leaks**: Do not store `Context` or `UIViewController` references long-term; use `LocalContext.current` or top-most controller resolution.
- **Blocking UI**: Perform contact queries efficiently.
- **Redundant Re-initialization**: Avoid re-registering launchers or delegates on every recomposition.

## Code Examples

### Initializing State (Common)
```kotlin
val contactPicker = rememberContactPickerState { contact ->
    println("Selected: ${contact?.name}")
}
```

### Actual Implementation (Android)
```kotlin
@Composable
actual fun RegisterContactPicker(onContactSelected: (Contact?) -> Unit) {
    if (::picker.isInitialized) return
    val context = LocalContext.current
    picker = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        // Process URI
    }
}
```

### Memory Copy (iOS)
```kotlin
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    memScoped {
        memcpy(bytes.refTo(0), this@toByteArray.bytes, this@toByteArray.length)
    }
    return bytes
}
```
