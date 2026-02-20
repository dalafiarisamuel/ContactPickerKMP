# ContactPicker 📇

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dalafiarisamuel/contactpicker)](https://central.sonatype.com/artifact/io.github.dalafiarisamuel/contactpicker)
[![Binary Compatibility](https://github.com/dalafiarisamuel/ContactPickerKMP/actions/workflows/validate-binary.yml/badge.svg?branch=master)](https://github.com/dalafiarisamuel/ContactPickerKMP/actions/workflows/validate-binary.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**ContactPicker** is a Kotlin Multiplatform (KMP) library that provides a native contact selection experience for Android and iOS using Jetpack Compose Multiplatform.

## ✨ Features

- 📱 **Native Experience**: Uses `PickContact` contract on Android and `CNContactPickerViewController` on iOS.
- 🧩 **Compose Multiplatform**: Easy-to-use Composable API with `rememberContactPickerState()`.
- 🖼️ **Avatar Support**: Retrieve and display contact profile pictures across platforms.
- 📂 **Rich Data**: Access names, multiple phone numbers, and email addresses.
- 🚀 **Type-Safe**: Clean, immutable `Contact` data model.

## 📚 Documentation

Full API documentation and guides are available at: [https://dalafiarisamuel.github.io/ContactPickerKMP/](https://dalafiarisamuel.github.io/ContactPickerKMP/)

---

## 🚀 Installation

Add the dependency to your `commonMain` source set in your `build.gradle.kts` file:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.dalafiarisamuel:contactpicker:0.2.0")
        }
    }
}
```

---

## 🛠 Usage

### 1. Platform Permissions

Before launching the picker, ensure you have declared the necessary permissions.

#### Android
Add this to your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

#### iOS
Add this to your `Info.plist`:
```xml
<key>NSContactsUsageDescription</key>
<string>Contacts permission is required to access your contacts to help you find friends.</string>
```

### 2. Implementation in Compose

```kotlin
import com.devtamuno.kmp.contactpicker.rememberContactPickerState
import com.devtamuno.kmp.contactpicker.extension.toPlatformImageBitmap

@Composable
fun ContactPickerScreen() {
    
    // 1. Initialize the state
    val contactPicker = rememberContactPickerState { contact ->
        // Optional callback: triggered when a contact is selected
        println("Selected: ${contact?.name}")
    }

    // 2. Observe the selected contact
    val selectedContact by contactPicker.value
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 3. Trigger the native picker
        Button(onClick = { contactPicker.launchContactPicker() }) {
            Text("Pick a Contact")
        }

        selectedContact?.let { contact ->
            Text("Name: ${contact.name}")
            
            // Display Avatar if available
            contact.contactAvatar?.toPlatformImageBitmap()?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "Contact Avatar",
                    modifier = Modifier.size(100.dp).clip(CircleShape)
                )
            }
        }
    }
}
```

---

## 🤝 Contributing

Contributions are welcome! If you find a bug or have a feature request, please open an [issue](https://github.com/dalafiarisamuel/ContactPickerKMP/issues) or submit a pull request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
