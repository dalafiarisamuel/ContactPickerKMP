# ContactPicker

[![Maven Central](https://img.shields.io/maven-central/v/io.github.dalafiarisamuel/contactpicker)](https://central.sonatype.com/artifact/io.github.dalafiarisamuel/contactpicker)
[![Binary Compatibility](https://github.com/dalafiarisamuel/ContactPickerKMP/actions/workflows/validate-binary.yml/badge.svg?branch=master)](https://github.com/dalafiarisamuel/ContactPickerKMP/actions/workflows/validate-binary.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**ContactPicker** is a Kotlin Multiplatform (KMP) library that provides a native contact selection experience for Android and iOS using Jetpack Compose Multiplatform.

## Features

- **Native Experience**: Uses `PickContact` contract on Android and `CNContactPickerViewController` on iOS.
- **Compose Multiplatform**: Easy-to-use Composable API with `rememberContactPickerState()` and `rememberMultiContactPickerState()`.
- **Multi-Selection Support**: Select multiple contacts at once. Supports the native `ACTION_PICK_CONTACTS` on Android 17 (API 37+) and native `CNContactPickerViewController` on iOS.
- **Backward Compatibility**: Provides a consistent, custom checkbox-based UI for multi-selection on Android versions prior to API 37.
- **State Management**: Reactive state handling with built-in `clear()` support to reset selections.
- **Avatar Support**: Retrieve and display contact profile pictures across platforms.
- **Rich Data**: Access names, multiple phone numbers, and email addresses.
- **Type-Safe**: Clean, immutable `Contact` data model.

## Screenshots

| Android | iOS |
| :---: | :---: |
| <img src="images/android_impl.png" width="50%" /> | <img src="images/ios_impl.png" width="50%" /> |
| <img src="images/android_multi-selection.png" width="50%" /> | <img src="images/ios_multi_selection.png" width="50%" /> |

## Documentation

Full API documentation and guides are available at: [https://dalafiarisamuel.github.io/ContactPickerKMP/](https://dalafiarisamuel.github.io/ContactPickerKMP/)

---

## Installation

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

## Usage

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

#### Single Selection
```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
    val contactSelected by contactPicker.value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 3. Trigger the native picker
        Button(onClick = { contactPicker.launchContactPicker() }) {
            Text("Pick a Contact")
        }

        contactSelected?.let { contact ->
            Spacer(modifier = Modifier.height(20.dp))
            Text("Name: ${contact.name}")

            // To display contact image, import `toPlatformImageBitmap()` extension 
            // function from `com.devtamuno.kmp.contactpicker.extension` package.
            // If there's no contact image, `contactAvatar` will be null.
            contact.contactAvatar?.toPlatformImageBitmap()?.let { imageBitmap ->
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Contact Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp).clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { contactPicker.clear() }) {
            Text("Clear Selection")
        }
    }
}
```

#### Multiple Selection
```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.devtamuno.kmp.contactpicker.rememberMultiContactPickerState
import com.devtamuno.kmp.contactpicker.extension.toPlatformImageBitmap

@Composable
fun MultiPickerScreen() {

    // 1. Initialize the state
    val multiContactPicker = rememberMultiContactPickerState { contacts ->
        // Optional callback: triggered when contacts are selected
        println("Selected ${contacts.size} contacts")
    }

    // 2. Observe the selected contacts
    val contactsSelected by multiContactPicker.value

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. Trigger the native picker
        Button(onClick = { multiContactPicker.launchContactPicker() }) {
            Text("Pick Multiple Contacts")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selected Contacts (${contactsSelected.size})",
            style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Displaying the list of selected contacts with their avatars
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(contactsSelected) { contact ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display avatar if available
                    contact.contactAvatar?.toPlatformImageBitmap()?.let { imageBitmap ->
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Contact Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = contact.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { multiContactPicker.clear() }) {
            Text("Clear Selection")
        }
    }
}
```

---

## Contributing

Contributions are welcome! If you find a bug or have a feature request, please open an [issue](https://github.com/dalafiarisamuel/ContactPickerKMP/issues) or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
