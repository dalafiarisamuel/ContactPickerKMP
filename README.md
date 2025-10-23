# ContactPicker [![Maven Central](https://img.shields.io/maven-central/v/io.github.dalafiarisamuel/contactpicker)](https://central.sonatype.com/artifact/io.github.dalafiarisamuel/contactpicker)

The ContactPicker is a component written in Kotlin Multiplatform that natively implements selecting a contact
for Android and iOS.

# How to use:

- In build.gradle file, add this dependency
    ````
    commonMain.dependencies {
        implementation("io.github.dalafiarisamuel:contactpicker:0.2.0")
    }
    ````

# Code sample:

1. Read Contacts Permission
    #### Android
    On Android you need to add the following permission to your `AndroidManifest.xml` file:
    
    ```xml
    <!-- For Read Contacts -->
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    ```
    
    #### iOS
    On iOS you need to add the following key to your `Info.plist` file:
    
    ```xml
    <key>NSContactsUsageDescription</key>
    <string>Contacts permission is required to access user's contacts</string>
    ```
    
    The string value is the message that will be displayed to the user when the permission is requested.

2. Using `rememberContactPickerState()` composable

    ```kotlin
    //in your commonMain package
    @Composable
    fun ContactPickerComponent() {
        
        val contactPicker = rememberContactPickerState {
            println(it)
        }
    
        val contactSelected by contactPicker.value
        
        //handle platform specific contact permission before calling launchContactPicker()
    
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { contactPicker.launchContactPicker() }) {
                Text("Pick Contact!")
            }
    
            Spacer(modifier = Modifier.padding(20.dp))
    
            Text("Selected Contact: ${contactSelected?.name}")
   
        // to display contact image, import `toPlatformImageBitmap()` extension function from `com.devtamuno.kmp.contactpicker.extension` package
        // if there's no contact image, `contactSelected?.contactAvatar` will be null
        
        contactSelected?.contactAvatar?.toPlatformImageBitmap()?.let { imageBitmap ->
            Spacer(modifier = Modifier.padding(20.dp))
            Image(
                 contentScale = ContentScale.Fit,
                 modifier = Modifier.fillMaxWidth()
                     .height(200.dp),
                 bitmap = imageBitmap,
                 contentDescription = null
            )
        }
    
        }
    }
    ```

