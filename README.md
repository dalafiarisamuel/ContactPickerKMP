# ContactPicker [![Maven Central](https://img.shields.io/maven-central/v/io.github.dalafiarisamuel/contactpicker)](https://central.sonatype.com/artifact/io.github.dalafiarisamuel/contactpicker)

The ContactPicker is a component written in Kotlin Multiplatform that natively implements selecting a contact
for Android and iOS.

# How to use:

- In build.gradle file, add this dependency
````
commonMain.dependencies {
    implementation("io.github.dalafiarisamuel:contactpicker:0.1.1")
}
````

# Code sample:

1. Using `rememberContactPickerState()` composable

```kotlin
@Composable
fun ContactPickerComponent() {
    
    //in your commonMain package
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

    }
}

```

