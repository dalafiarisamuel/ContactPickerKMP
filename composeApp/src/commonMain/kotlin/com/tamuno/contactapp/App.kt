package com.tamuno.contactapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devtamuno.kmp.contactpicker.rememberContactPickerState
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        //val writeStorage = rememberPermissionState()

        val contactPicker = rememberContactPickerState {
            println(it)
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { contactPicker.launchContactPicker() }) {
                Text("Click me!")
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Text("Selected Contact: ${contactPicker.value.value?.name}")

        }
    }
}